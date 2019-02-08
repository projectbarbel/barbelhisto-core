package com.projectbarbel.histo;

import java.util.List;
import java.util.function.BiFunction;

import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public class BarbelHistoFactory {

    private BarbelHistoContext context;

    public BarbelHistoFactory(BarbelHistoContext context) {
        this.context = context;
    }

    public BarbelHistoFactory create(BarbelHistoContext context) {
        return new BarbelHistoFactory(context);
    }

    public BiFunction<DocumentJournal, Bitemporal, List<Object>> createJournalUpdateStrategy() {
        return context.getJournalUpdateStrategy().apply(context);
    }
    
}
