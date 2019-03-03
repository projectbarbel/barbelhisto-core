package org.projectbarbel.histo.extension;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.DocumentJournal.Inactivation;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.OnLoadOperationEvent;
import org.projectbarbel.histo.event.EventType.UnLoadOperationEvent;
import org.projectbarbel.histo.event.EventType.UpdateFinishedEvent;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;

/**
 * Mongo shadow update listener implementation to synchronize
 * {@link BarbelHisto} backbone updates to external data source.
 * 
 * @author Niklas Schlimm
 * @param <R> data resource to access
 * @param <T> document type to store
 *
 */
public abstract class AbstractMongoUpdateListener<R, T> implements UpdateListenerProtocol<R, T> {

    protected static final String VERSION_ID = ".versionId";
    protected R shadow;
    protected final BarbelMode mode;
    protected final Gson gson;
    protected String versionIdFieldName;
    protected final Class<?> managedType;
    protected final String documentIdFieldName;
    protected final Class<? extends Bitemporal> persistedType;

    public AbstractMongoUpdateListener(Class<?> managedType, Gson gson) {
        this.managedType = managedType;
        this.gson = gson;
        if (Bitemporal.class.isAssignableFrom(managedType)) {
            mode = BarbelMode.BITEMPORAL;
        } else {
            mode = BarbelMode.POJO;
        }
        this.versionIdFieldName = mode.getStampFieldName(mode.getPersistenceObjectType(managedType),
                BitemporalStamp.class) + VERSION_ID;
        this.documentIdFieldName = mode.getDocumentIdFieldNameOnPersistedType(managedType);
        this.persistedType = mode.getPersistenceObjectType(managedType);
    }

    @Subscribe
    public void handleInitialization(BarbelInitializedEvent event) {
        try {
            shadow = createResource();
        } catch (Exception e) {
            event.failed(e);
        }
    }

    @Subscribe
    public void handleLoadOperation(OnLoadOperationEvent event) {
        try {
            @SuppressWarnings("unchecked")
            Collection<Bitemporal> managedBitemporalsToLoad = (Collection<Bitemporal>) event.getEventContext()
                    .get(OnLoadOperationEvent.DATA);
            if (managedBitemporalsToLoad.size() == 0)
                return;
            for (Bitemporal bitemporal : managedBitemporalsToLoad) {
                if (queryJournal(bitemporal.getBitemporalStamp().getDocumentId()).iterator().hasNext())
                    throw new IllegalStateException("document with id exists: "
                            + bitemporal.getBitemporalStamp().getDocumentId() + " - unable to load");
            }
            // @formatter:off
            List<T> documentsToInsert = managedBitemporalsToLoad.stream()
                    .map(mode::managedBitemporalToPersistenceObject) // to persistence object
                    .map(gson::toJson) // to json
                    .map(d->fromJsonToStoredDocument(d)) // to mongo Document
                    .collect(Collectors.toList()); // to list
            // @formatter:on
            insertDocuments(documentsToInsert);
        } catch (Exception e) {
            event.failed(e);
        }
    }

    @Subscribe
    public void handleUnLoadOperation(UnLoadOperationEvent event) {
        try {
            BarbelHisto<?> histo = (BarbelHisto<?>) event.getEventContext().get(UnLoadOperationEvent.BARBEL);
            Object[] documentIds = (Object[]) event.getEventContext().get(UnLoadOperationEvent.DOCUMENT_IDs);
            for (Object id : documentIds) {
                List<Bitemporal> docs = StreamSupport
                        .stream(queryJournal(id).spliterator(), true)
                        .map(d -> fromStoredDocumentToPersistedType(d)).collect(Collectors.toList());
                if (histo.contains(id))
                    ((BarbelHistoCore<?>) histo).unloadQuiet(id);
                ((BarbelHistoCore<?>) histo).loadQuiet(docs);
                deleteJournal(id);
            }
        } catch (Exception e) {
            event.failed(e);
        }
    }

    @Subscribe
    public void handleUpdate(UpdateFinishedEvent event) {
        try {
            @SuppressWarnings("unchecked")
            List<Bitemporal> managedBitemporalsInserted = (List<Bitemporal>) event.getEventContext()
                    .get(UpdateFinishedEvent.NEWVERSIONS);
            @SuppressWarnings("unchecked")
            Set<Inactivation> inactivations = (Set<Inactivation>) event.getEventContext()
                    .get(UpdateFinishedEvent.INACTIVATIONS);
            // delete first ! cause version id is the key for deletion, and replaced new
            // objects carry same version IDs
            List<Bitemporal> objectsRemoved = inactivations.stream()
                    .map(r -> mode.managedBitemporalToPersistenceObject(r.getObjectRemoved()))
                    .collect(Collectors.toList());
            List<Long> results = objectsRemoved.stream().map(objectToRemove -> delete(objectToRemove.getBitemporalStamp().getVersionId()))
                    .collect(Collectors.toList());
            Validate.validState(results.stream().filter(r -> r != 1).count() == 0,
                    "delete operation failed - delete count must always be = 1");
            // // @formatter:off
            List<T> documentsToInsert = managedBitemporalsInserted.stream()
                    .map(mode::managedBitemporalToPersistenceObject) // to persistence object
                    .map(gson::toJson) // to json
                    .map(json -> fromJsonToStoredDocument(json)) // to stored type
                    .collect(Collectors.toList()); // to list
            List<T> documentsAddedOnReplacements = inactivations.stream()
                    .map(r -> mode.managedBitemporalToPersistenceObject(r.getObjectAdded())) // to persistence objects
                    .map(gson::toJson) // to json
                    .map(json -> fromJsonToStoredDocument(json)) // to stored type
                    .collect(Collectors.toList()); // to list
            documentsToInsert.addAll(documentsAddedOnReplacements);
            // @formatter:on
            insertDocuments(documentsToInsert);
        } catch (Exception e) {
            event.failed(e);
        }
    }

    @Override
    public abstract R createResource();
    
    @Override
    public abstract void insertDocuments(List<T> documentsToInsert);
    
    @Override
    public abstract Iterable<T> queryJournal(Object documentId);
    
    @Override
    public abstract long delete(String versionId);

    @Override
    public abstract long deleteJournal(Object id);

    @Override
    public Bitemporal fromStoredDocumentToPersistedType(T document) {
        String json = fromStroredDocumentToJson(document);
        Bitemporal object = (Bitemporal)gson.fromJson(json, persistedType);
        if (object instanceof BitemporalVersion) {
            BitemporalVersion bv = (BitemporalVersion) object;
            bv.setObject(gson.fromJson(gson.toJsonTree(bv.getObject()).toString(), managedType));
        }
        return object;
    }

    @Override
    public abstract String fromStroredDocumentToJson(T document);
        
    @Override
    public abstract T fromJsonToStoredDocument(String json);
    
}