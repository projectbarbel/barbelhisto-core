package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.Bitemporal;

public class ReaderFunctionGetEffectiveByDate<T extends Bitemporal<?>>
        implements BiFunction<IndexedCollection<T>, LocalDate, Optional<T>> {

    @Override
    public Optional<T> apply(IndexedCollection<T> journal, LocalDate day) {
        Validate.notNull(journal, "null value for journal not welcome here");
        List<T> documents = (List<T>) journal.stream().collect(Collectors.toList());
        JournalPredicates predicates = new JournalPredicates(day);
        IndexedCollection<T> currentVersions = documents.stream().filter(predicates::isActive).filter(predicates::effectiveOn)
                .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        if (currentVersions.size() == 0)
            return Optional.empty();
        else if (currentVersions.size() == 1)
            return Optional.of(currentVersions.iterator().next());
        else
            throw new IllegalStateException("too many current active versions - insonsistent journal");
    }
}
