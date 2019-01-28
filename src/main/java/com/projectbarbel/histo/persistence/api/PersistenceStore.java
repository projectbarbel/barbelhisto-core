package com.projectbarbel.histo.persistence.api;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public interface PersistenceStore<T extends Bitemporal<?>> {

    DocumentJournal<?> loadJournal(String documentId);
    long updateJournal(DocumentJournal<?> journal);

}
