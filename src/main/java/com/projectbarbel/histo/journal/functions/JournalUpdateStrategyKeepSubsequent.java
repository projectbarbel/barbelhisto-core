package com.projectbarbel.histo.journal.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class JournalUpdateStrategyKeepSubsequent<T extends Bitemporal> implements BiFunction<DocumentJournal<T>,VersionUpdateResult<T>, List<T>> {

    private final String user;
    public JournalUpdateStrategyKeepSubsequent(String user) {
        this.user = user;
    }
    
    @Override
    public List<T> apply(DocumentJournal<T> journal, VersionUpdateResult<T> update) {
        List<T> newVersions = new ArrayList<T>();
        Optional<T> interruptedVersion = journal.read().effectiveTime().effectiveAt(update.effectiveFrom());
        interruptedVersion.ifPresent(d -> d.getBitemporalStamp().inactivatedCopy(user));
        newVersions.add(update.newPrecedingVersion());
        newVersions.add(update.newSubsequentVersion());
        return newVersions;
    }

}
