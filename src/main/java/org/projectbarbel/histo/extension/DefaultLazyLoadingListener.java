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

    public static IndexedCollection<BitemporalVersion> shadow;

    protected DefaultLazyLoadingListener(Class<?> managedType, Gson gson, boolean singletonContext,
            IndexedCollection<BitemporalVersion> shadow) {
        super(managedType, gson, singletonContext);
        DefaultLazyLoadingListener.shadow = shadow;
    }

    @Override
    public Iterable<BitemporalVersion> queryAll() {
        return shadow;
    }

    @Override
    public Iterable<BitemporalVersion> queryJournal(Object id) {
        return shadow.stream().filter(d -> d.getBitemporalStamp().getDocumentId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public IndexedCollection<BitemporalVersion> getExternalDataResource() {
        return shadow;
    }

    @Override
    public String fromStoredDocumentToPersistenceObjectJson(BitemporalVersion document) {
        return gson.toJson(document.getObject());
    }

}
