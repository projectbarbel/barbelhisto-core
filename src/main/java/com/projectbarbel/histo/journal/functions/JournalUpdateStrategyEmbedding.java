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

public class JournalUpdateStrategyEmbedding<T>
        implements BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> {

    private final BarbelHistoContext<T> context;

    public JournalUpdateStrategyEmbedding(BarbelHistoContext<T> context) {
        this.context = context;
    }

    @Override
    public List<T> apply(DocumentJournal<T> journal, final VersionUpdateResult<T> update) {
        List<T> newVersions = new ArrayList<>();
        Optional<T> interruptedFromVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        Optional<T> interruptedUntilVersion = journal.read().effectiveTime().effectiveAt(update.effectiveUntil());
        if (interruptedUntilVersion.isPresent()) {
            VersionUpdateResult<T> result = context.getBarbelFactory().createVersionUpdate(interruptedUntilVersion.get()).prepare()
                    .effectiveFrom(update.effectiveUntil()).until(((Bitemporal)interruptedUntilVersion.get()).getBitemporalStamp().getEffectiveTime().until()).execute();
            ((Bitemporal)interruptedUntilVersion.get()).getBitemporalStamp().inactivatedCopy(context.getUser());
            newVersions.add(result.newSubsequentVersion());
        }
        IndexedCollection<T> betweenVersions = journal.read().effectiveTime()
                .effectiveBetween(((Bitemporal)update.newSubsequentVersion()).getBitemporalStamp().getEffectiveTime());
        betweenVersions.stream().forEach(d->((Bitemporal)d).getBitemporalStamp().inactivatedCopy(context.getUser()));
        interruptedFromVersion.ifPresent(d->((Bitemporal)d).getBitemporalStamp().inactivatedCopy(context.getUser()));
        newVersions.add(update.newPrecedingVersion());
        newVersions.add(update.newSubsequentVersion());
        return newVersions;
    }

}
