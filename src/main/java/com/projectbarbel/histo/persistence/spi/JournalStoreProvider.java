package com.projectbarbel.histo.persistence.spi;

import com.projectbarbel.histo.api.DocumentJournal;

public interface JournalStoreProvider {

    DocumentJournal<?> loadJournal(String documentId);
    long persistJournal(DocumentJournal<?> journal);

}
