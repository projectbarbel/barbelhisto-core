package com.projectbarbel.histo.functions.journal;

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
    public IndexedCollection<T> apply(IndexedCollection<T> documentJournal, EffectivePeriod intervall) {
        Validate.notNull(documentJournal, "null value for journal not welcome here");
        return BitemporalCollectionPreparedStatements
                .getActiveVersionsEffectiveBetween_ByFromAndUntilDate_orderByEffectiveFrom(documentJournal,
                        intervall.from(), intervall.until())
                .stream().collect(Collectors.toCollection(ConcurrentIndexedCollection::new));

    }
}
