package com.projectbarbel.histo.functions.journal;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.EffectivePeriod;

public class ReaderFunctionGetEffectiveBetween<T extends Bitemporal<?>>
        implements BiFunction<IndexedCollection<T>, EffectivePeriod, IndexedCollection<T>> {

    @Override
    public IndexedCollection<T> apply(IndexedCollection<T> journal, EffectivePeriod intervall) {
        Validate.notNull(journal, "null value for journal not welcome here");
        JournalPredicates predicates = new JournalPredicates(intervall);
        List<T> documents = (List<T>) journal.stream().collect(Collectors.toList());
        return documents.stream().filter(predicates::isActive).filter(predicates::effectiveBetween)
                .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));

    }
}
