package com.projectbarbel.histo.functions.journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class JournalUpdateStrategyInactivateSubsequent<T extends Bitemporal<?>> implements BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> {

    @Override
    public List<T> apply(DocumentJournal<T> journal, VersionUpdateResult<T> update) {
        List<T> newVersions = new ArrayList<T>();
        Optional<T> interruptedVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        interruptedVersion.ifPresent(Bitemporal::inactivate);
        journal.read().effectiveTime().effectiveAfter(update.effectiveFrom()).stream().forEach((d)->d.inactivate());
        newVersions.add(update.newPrecedingVersion());
        newVersions.add(update.newSubsequentVersion());
        return newVersions;
    }

}
