package com.projectbarbel.histo.functions;

import java.util.function.Supplier;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.BitemporalVersion;

public class DefaultPersistenceSupplier
        implements Supplier<IndexedCollection<BitemporalVersion>> {

    @Override
    public IndexedCollection<BitemporalVersion> get() {
        return new ConcurrentIndexedCollection<BitemporalVersion>();
    }

}
