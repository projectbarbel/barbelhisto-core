package com.projectbarbel.histo;

import com.projectbarbel.histo.functions.JournalUpdateStrategyEmbedding.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
