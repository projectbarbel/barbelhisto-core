package org.projectbarbel.histo.extension;

import java.util.List;
import java.util.stream.Collectors;

import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.model.Bitemporal;
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
public class DefaultUpdateListener extends AbstractUpdateListener<IndexedCollection<BitemporalVersion>, BitemporalVersion> {
    private Gson gson = BarbelHistoContext.getDefaultGson();

    public DefaultUpdateListener(Class<?> managedType, Gson gson) {
        super(managedType, gson);
    }

    @Override
    public IndexedCollection<BitemporalVersion> getExternalDataResource() {
        return DefaultLazyLoadingListener.getShadow();
    }

    @Override
    public void insertDocuments(List<BitemporalVersion> documentsToInsert) {
        shadow.addAll(documentsToInsert);
    }

    @Override
    public Iterable<BitemporalVersion> queryJournal(Object documentId) {
        return shadow.stream().filter(bv -> bv.getBitemporalStamp().getDocumentId().equals(documentId))
                .collect(Collectors.toList());
    }

    @Override
    public long delete(String versionId) {
        return shadow.remove(shadow.stream().filter(bv -> bv.getBitemporalStamp().getVersionId().equals(versionId))
                .findFirst().orElseThrow(()->new IllegalStateException("cannot find version"))) ? 1 : 0;
    }

    @Override
    public long deleteJournal(Object id) {
        return shadow.removeIf(bv -> bv.getBitemporalStamp().getDocumentId().equals(id)) ? 1 : 0;
    }

    @Override
    public String fromStroredDocumentToPersistenceObjectJson(BitemporalVersion document) {
        return gson.toJson(document.getObject(), mode.getPersistenceObjectType(managedType));
    }

    @Override
    public BitemporalVersion fromPersistenceObjectJsonToStoredDocument(String json) {
        Bitemporal object = (Bitemporal) gson.fromJson(json, mode.getPersistenceObjectType(managedType));
        return new BitemporalVersion(object.getBitemporalStamp(), object);
    }
}