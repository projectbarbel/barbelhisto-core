package com.projectbarbel.histo.persistence;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.model.Bitemporal;

public interface BarbelHistoRepository {

    long loadCache(RepositoryOperationOptions options);

    long persistCache(RepositoryOperationOptions options);

    IndexedCollection<Bitemporal> getLocalCache();

    default void add(Bitemporal bitemporal) {
        getLocalCache().add(bitemporal);
    }
    
    default void add(Bitemporal... bitemporals) {
        getLocalCache().addAll(Arrays.asList(bitemporals));
    }
    
    default void populateCache(Collection<Bitemporal> bitemporals) {
        getLocalCache().addAll(bitemporals);
    }

    default IndexedCollection<Bitemporal> retrieveCached(Query<Bitemporal> query) {
        return getLocalCache().retrieve(query).stream()
                .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
    }

    default IndexedCollection<Bitemporal> retrieveCached(Query<Bitemporal> query, QueryOptions options) {
        return getLocalCache().retrieve(query, options).stream()
                .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
    }

}
