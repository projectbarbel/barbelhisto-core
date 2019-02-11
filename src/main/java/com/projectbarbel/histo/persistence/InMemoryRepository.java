package com.projectbarbel.histo.persistence;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.Bitemporal;

public class InMemoryRepository implements BarbelHistoRepository {

    @Override
    public long loadCache(RepositoryOperationOptions options) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long persistCache(RepositoryOperationOptions options) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IndexedCollection<Bitemporal> getLocalCache() {
        // TODO Auto-generated method stub
        return null;
    }


}
