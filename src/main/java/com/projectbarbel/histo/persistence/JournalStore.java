package com.projectbarbel.histo.persistence;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public interface JournalStore<T extends Bitemporal<?>> {

    DocumentJournal<T> loadJournal(String documentId);
    long persistJournal(DocumentJournal<T> journal);
    
}
