package org.projectbarbel.histo.model;

import org.projectbarbel.histo.functions.EbeddingJournalUpdateStrategy.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
