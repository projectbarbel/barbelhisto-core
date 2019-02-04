package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.Bitemporal;

public class ReaderFunctionGetEffectiveAfter<T extends Bitemporal<?>>
        implements BiFunction<IndexedCollection<T>, LocalDate, IndexedCollection<T>> {

    @Override
    public IndexedCollection<T> apply(IndexedCollection<T> journal, LocalDate day) {
        Validate.notNull(journal, "null value for journal not welcome here");
        JournalPredicates predicates = new JournalPredicates(day);
        List<T> documents = (List<T>) journal.stream().collect(Collectors.toList());
        return documents.stream().filter(predicates::isActive).filter(predicates::effectiveAfter)
                .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));

    }
}
