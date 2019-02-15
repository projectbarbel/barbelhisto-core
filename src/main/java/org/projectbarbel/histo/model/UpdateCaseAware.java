package org.projectbarbel.histo.model;

import org.projectbarbel.histo.functions.DefaultJournalUpdateStrategy.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
