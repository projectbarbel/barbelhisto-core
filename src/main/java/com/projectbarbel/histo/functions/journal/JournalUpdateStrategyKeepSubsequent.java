package com.projectbarbel.histo.functions.journal;

import java.util.Optional;
import java.util.function.BiFunction;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class JournalUpdateStrategyKeepSubsequent<T extends Bitemporal<?>> implements BiFunction<DocumentJournal<T>,VersionUpdateResult<T>, DocumentJournal<T>> {

    @Override
    public DocumentJournal<T> apply(DocumentJournal<T> journal, VersionUpdateResult<T> update) {
        Optional<T> interruptedVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        interruptedVersion.ifPresent(Bitemporal::inactivate);
        journal.add(update::newPrecedingVersion);
        journal.add(update::newSubsequentVersion);
        return journal;
    }

}
