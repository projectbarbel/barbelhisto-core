package org.projectbarbel.histo.model;

import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
