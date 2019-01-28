package com.projectbarbel.histo.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.projectbarbel.histo.functions.KeepSubsequentUpdateStrategy;
import com.projectbarbel.histo.model.Bitemporal;

public class DocumentJournal<T extends Bitemporal<?>> {

    private final List<T> journal = new ArrayList<T>();
    private static final Logger logger = Logger.getLogger(DocumentJournal.class.getName());
    @SuppressWarnings("unused")
    private final Function<DocumentJournal<?>,DocumentJournal<?>> journalUpdateStrategy = new KeepSubsequentUpdateStrategy();

    private DocumentJournal(List<T> documentList) {
        super();
        documentList.stream().forEach(journal::add);
    }

    public int size() {
        return journal.size();
    }

    public List<? extends Bitemporal<?>> list() {
        return journal.stream().collect(Collectors.toCollection(ArrayList::new));
    }

    @SuppressWarnings("unchecked")
    public static <T extends DocumentJournal<O>, O extends Bitemporal<?>> T create(List<O> listOfBitemporal) {
        return (T)new DocumentJournal<O>(listOfBitemporal);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends DocumentJournal<O>, O extends Bitemporal<?>> T create(Bitemporal<?> newDocument) {
        return (T)new DocumentJournal<Bitemporal<?>>(Collections.singletonList(newDocument));
    }

    public DocumentJournal<T> sortAscendingByEffectiveDate() {
        Collections.sort(journal, (e1,e2)->e1.getEffectiveFrom().isBefore(e2.getEffectiveFrom())?-1:1);
        return this;
    }

    public void prettyPrint() {
        logger.log(Level.INFO, this.toString());
    }

}
