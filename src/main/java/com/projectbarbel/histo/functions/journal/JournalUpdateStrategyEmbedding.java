package com.projectbarbel.histo.functions.journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.api.VersionUpdate;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class JournalUpdateStrategyEmbedding<T extends Bitemporal<?>>
        implements BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> {

    @Override
    public List<T> apply(DocumentJournal<T> journal, final VersionUpdateResult<T> update) {
        List<T> newVersions = new ArrayList<>();
        Optional<T> interruptedFromVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        Optional<T> interruptedUntilVersion = journal.read().effectiveTime().effectiveAt(update.effectiveUntil());
        if (interruptedUntilVersion.isPresent()) {
            VersionUpdateResult<T> result = VersionUpdate.of(interruptedUntilVersion.get()).prepare()
                    .from(update.effectiveUntil()).until(interruptedUntilVersion.get().getEffectiveUntil()).execute();
            interruptedUntilVersion.get().inactivate();
            newVersions.add(result.newSubsequentVersion());
        }
        List<T> betweenVersions = journal.read().effectiveTime()
                .effectiveBetween(update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime());
        betweenVersions.stream().forEach(Bitemporal::inactivate);
        interruptedFromVersion.ifPresent(Bitemporal::inactivate);
        newVersions.add(update.newPrecedingVersion());
        newVersions.add(update.newSubsequentVersion());
        return newVersions;
    }

}
