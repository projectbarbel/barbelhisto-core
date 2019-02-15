package org.projectbarbel.histo;

import org.projectbarbel.histo.functions.DefaultJournalUpdateStrategy.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
