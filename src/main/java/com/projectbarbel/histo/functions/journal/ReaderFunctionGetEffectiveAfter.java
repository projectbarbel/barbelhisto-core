package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.Bitemporal;

public class ReaderFunctionGetEffectiveAfter<T extends Bitemporal<?>>
        implements BiFunction<IndexedCollection<T>, LocalDate, IndexedCollection<T>> {

    @Override
    public IndexedCollection<T> apply(IndexedCollection<T> documentJournal, LocalDate day) {
        Validate.notNull(documentJournal, "null value for journal not welcome here");
        return BitemporalCollectionPreparedStatements
                .getActiveVersionsEffectiveAfter_ByDate_orderByEffectiveFrom(documentJournal,day)
                .stream().collect(Collectors.toCollection(ConcurrentIndexedCollection::new));

    }
}
