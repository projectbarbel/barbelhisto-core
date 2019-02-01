package com.projectbarbel.histo.persistence.spi;

import com.projectbarbel.histo.persistence.api.DocumentJournal;

public interface JournalStoreProvider {

    DocumentJournal loadJournal(String documentId);
    long persistJournal(DocumentJournal journal);

}
