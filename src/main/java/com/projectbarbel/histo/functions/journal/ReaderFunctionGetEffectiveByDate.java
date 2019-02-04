package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;
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
    public Optional<T> apply(IndexedCollection<T> documentJournal, LocalDate day) {
        Validate.notNull(documentJournal, "null value for journal not welcome here");
        IndexedCollection<T> currentVersions = BitemporalCollectionPreparedStatements
                .getActiveVersionEffectiveOn_ByDate(documentJournal, day).stream()
                .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        if (currentVersions.size() == 0)
            return Optional.empty();
        else if (currentVersions.size() == 1)
            return Optional.of(currentVersions.iterator().next());
        else
            throw new IllegalStateException("too many current active versions - insonsistent journal");
    }
}
