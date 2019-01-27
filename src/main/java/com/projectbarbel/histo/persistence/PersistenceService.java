package com.projectbarbel.histo.persistence;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.Journal;

public interface PersistenceService<T extends Bitemporal<O>, O> {

    Journal<T, O> loadJournal(String documentId);
    long updateJournal(Journal<T, O> journal);
    
}
