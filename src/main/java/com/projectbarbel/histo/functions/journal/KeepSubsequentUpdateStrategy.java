package com.projectbarbel.histo.functions.journal;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.api.VersionUpdate;
import com.projectbarbel.histo.model.Bitemporal;

public class KeepSubsequentUpdateStrategy<T extends Bitemporal<?>> implements BiFunction<DocumentJournal<T>,VersionUpdate<T>, DocumentJournal<T>> {

    @Override
    public DocumentJournal<T> apply(DocumentJournal<T> journal, VersionUpdate<T> update) {
        Validate.validState(update.done());
        Optional<T> interruptedVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        interruptedVersion.ifPresent(Bitemporal::inactivate);
        journal.add(update.result().newPrecedingVersion());
        journal.add(update.result().newSubsequentVersion());
        return journal;
    }

}
