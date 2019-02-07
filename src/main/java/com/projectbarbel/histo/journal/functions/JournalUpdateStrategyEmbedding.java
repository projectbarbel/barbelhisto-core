package com.projectbarbel.histo.journal.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class JournalUpdateStrategyEmbedding
        implements BiFunction<DocumentJournal, VersionUpdateResult, List<Object>> {

    private final BarbelHistoContext context;

    public JournalUpdateStrategyEmbedding(BarbelHistoContext context) {
        this.context = context;
    }

    @Override
    public List<Object> apply(DocumentJournal journal, final VersionUpdateResult update) {
        List<Object> newVersions = new ArrayList<>();
        Optional<Bitemporal> interruptedFromVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        Optional<Bitemporal> interruptedUntilVersion = journal.read().effectiveTime().effectiveAt(update.effectiveUntil());
        if (interruptedUntilVersion.isPresent()) {
            VersionUpdateResult result = context.getBarbelFactory().createVersionUpdate(interruptedUntilVersion.get()).prepare()
                    .effectiveFrom(update.effectiveUntil()).until((interruptedUntilVersion.get()).getBitemporalStamp().getEffectiveTime().until()).execute();
            (interruptedUntilVersion.get()).getBitemporalStamp().inactivatedCopy(context.getUser());
            newVersions.add(result.newSubsequentVersion());
        }
        IndexedCollection<Bitemporal> betweenVersions = journal.read().effectiveTime()
                .effectiveBetween((update.newSubsequentVersion()).getBitemporalStamp().getEffectiveTime());
        betweenVersions.stream().forEach(d->d.getBitemporalStamp().inactivatedCopy(context.getUser()));
        interruptedFromVersion.ifPresent(d->d.getBitemporalStamp().inactivatedCopy(context.getUser()));
        newVersions.add(update.newPrecedingVersion());
        newVersions.add(update.newSubsequentVersion());
        return newVersions;
    }

}
