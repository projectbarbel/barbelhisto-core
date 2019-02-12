package com.projectbarbel.histo;

import com.projectbarbel.histo.functions.DefaultJournalUpdateStrategy.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
