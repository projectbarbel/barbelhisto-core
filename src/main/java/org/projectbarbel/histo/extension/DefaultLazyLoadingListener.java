package org.projectbarbel.histo.extension;

import java.util.stream.Collectors;

import org.projectbarbel.histo.model.BitemporalVersion;

import com.google.gson.Gson;
import com.googlecode.cqengine.IndexedCollection;

/**
 * Default listener implementation. Can be used to shadow backbone into a
 * persistent {@link IndexedCollection}.
 * 
 * @author Niklas Schlimm
 *
 */
public class DefaultLazyLoadingListener
        extends AbstractLazyLoadingListener<IndexedCollection<BitemporalVersion>, BitemporalVersion> {

    private static IndexedCollection<BitemporalVersion> shadowCollection;

    public static void setShadow(IndexedCollection<BitemporalVersion> shadow) {
        DefaultLazyLoadingListener.shadowCollection = shadow;
    }
    
    public static IndexedCollection<BitemporalVersion> getShadow() {
        return DefaultLazyLoadingListener.shadowCollection;
    }
    
    protected DefaultLazyLoadingListener(Class<?> managedType, Gson gson, boolean singletonContext,
            IndexedCollection<BitemporalVersion> shadow) {
        super(managedType, gson, singletonContext);
        DefaultLazyLoadingListener.shadowCollection = shadow;
    }

    @Override
    public Iterable<BitemporalVersion> queryAll() {
        return shadowCollection;
    }

    @Override
    public Iterable<BitemporalVersion> queryJournal(Object id) {
        return shadowCollection.stream().filter(d -> d.getBitemporalStamp().getDocumentId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public IndexedCollection<BitemporalVersion> getExternalDataResource() {
        return shadowCollection;
    }

    @Override
    public String fromStoredDocumentToPersistenceObjectJson(BitemporalVersion document) {
        return gson.toJson(document.getObject());
    }

}
