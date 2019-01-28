package com.projectbarbel.histo.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.functions.KeepSubsequentUpdateStrategy;
import com.projectbarbel.histo.model.Bitemporal;

public class DocumentJournal<T extends Bitemporal<?>> {

    private final List<T> journal = new ArrayList<T>();
    private static final Logger logger = Logger.getLogger(DocumentJournal.class.getName());
    @SuppressWarnings("unused")
    private final BiFunction<DocumentJournal<?>,VersionUpdate, DocumentJournal<?>> journalUpdateStrategy = new KeepSubsequentUpdateStrategy();

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
    public static <T extends DocumentJournal<O>, O extends Bitemporal<?>> T create(List<O> listOfBitemporalDocuments) {
        Validate.notNull(listOfBitemporalDocuments, "new document list must not be null when creating new journal");
        Validate.isTrue(listOfBitemporalDocuments.size()>0, "list of documents must not be empty");
        return (T)new DocumentJournal<O>(listOfBitemporalDocuments);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends DocumentJournal<O>, O extends Bitemporal<?>> T create(Bitemporal<?> newDocument) {
        Validate.notNull(newDocument, "new document must not be null when creating new journal");
        return (T)new DocumentJournal<Bitemporal<?>>(Collections.singletonList(newDocument));
    }

    public DocumentJournal<T> sortAscendingByEffectiveDate() {
        Collections.sort(journal, Comparator.comparing(Bitemporal::getEffectiveFrom));
        return this;
    }

    public void prettyPrint() {
        logger.log(Level.INFO, this.toString());
    }

}
