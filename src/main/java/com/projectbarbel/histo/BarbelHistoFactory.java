package com.projectbarbel.histo;

import java.util.function.BiConsumer;

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

    public BiConsumer<DocumentJournal, Bitemporal> createJournalUpdateStrategy() {
        return context.getJournalUpdateStrategy().apply(context);
    }
    
}
