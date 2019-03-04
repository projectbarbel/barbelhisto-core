package org.projectbarbel.histo.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.InitializeJournalEvent;
import org.projectbarbel.histo.event.EventType.RetrieveDataEvent;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.google.gson.Gson;
import com.googlecode.cqengine.query.Query;

/**
 * Mongo shadow lazy loading listener implementation to pre-fetch saved data
 * from previous sessions back to {@link BarbelHisto}. <br>
 * 
 * The simple listener implementations provide support for all
 * {@link BarbelQueries} and all custom queries as long they use the
 * {@link BarbelQueries#DOCUMENT_ID} as a filter criterion.
 * 
 * If you run an instance of {@link BarbelHisto} as global singleton in your
 * application set-up, set the <code>singletonContext</code> flag to true. This
 * will increase performance as data is the listener refuses to refresh data on
 * each retrieve operation. This is safe, if only ever one {@link BarbelHisto}
 * instance is using the accessed data resource.
 * 
 * @author Niklas Schlimm
 * @param <R> the data resource to access
 * @param <T> the type stored in the resource
 *
 */
public abstract class AbstractLazyLoadingListener<R,T> implements LazyLoadingListenerProtocol<R, T>{

    protected final Class<?> managedType;
    protected final Class<?> persistedType;
    protected final BarbelMode mode;
    protected final Gson gson;
    protected final boolean singletonContext;
    protected final String documentIdFieldName;
    protected R shadow;

    protected AbstractLazyLoadingListener(Class<?> managedType, Gson gson, boolean singletonContext) {
        if (Bitemporal.class.isAssignableFrom(managedType))
            mode = BarbelMode.BITEMPORAL;
        else
            mode = BarbelMode.POJO;
        this.documentIdFieldName = mode.getDocumentIdFieldNameOnPersistedType(managedType);
        this.managedType = managedType;
        this.gson = gson;
        this.singletonContext = singletonContext;
        this.persistedType = mode.getPersistenceObjectType(managedType);
    }

    public void handleInitialization(BarbelInitializedEvent event) {
        try {
            shadow = getExternalDataResource();
        } catch (Exception e) {
            event.failed(e);
        }
    }

    public void handleRetrieveData(RetrieveDataEvent event) {
        try {
            Query<?> query = (Query<?>) event.getEventContext().get(RetrieveDataEvent.QUERY);
            BarbelHistoCore<?> histo = (BarbelHistoCore<?>) event.getEventContext().get(RetrieveDataEvent.BARBEL);
            final List<Object> ids = BarbelQueries.returnIDsForQuery(query, new ArrayList<>());
            if (!ids.isEmpty()) {
                for (Object id : ids) {
                    if (!histo.contains(id) || (histo.contains(id) && !singletonContext)) {
                        List<Bitemporal> docs = StreamSupport
                                .stream(queryJournal(id).spliterator(), true)
                                .map(d -> fromStoreDocumentPersistenceObject((T) d)).collect(Collectors.toList());
                        if (histo.contains(id))
                            ((BarbelHistoCore<?>)histo).unloadQuiet(id);
                        ((BarbelHistoCore<?>)histo).loadQuiet(docs);
                    }
                }
            } else {
                // literally the complete refresh with backbone data
                List<Bitemporal> docs = StreamSupport.stream(queryAll().spliterator(), true)
                        .map(d -> fromStoreDocumentPersistenceObject((T) d)).collect(Collectors.toList());
                histo.getContext().getBackbone().clear();
                ((BarbelHistoCore<?>)histo).loadQuiet(docs);
            }
        } catch (Exception e) {
            event.failed(e);
        }
    }

    public void handleInitializeJournal(InitializeJournalEvent event) {
        try {
            DocumentJournal journal = (DocumentJournal) event.getEventContext().get(DocumentJournal.class);
            BarbelHisto<?> histo = (BarbelHisto<?>) event.getEventContext().get(RetrieveDataEvent.BARBEL);
            if (!histo.contains(journal.getId()) || (histo.contains(journal.getId()) && !singletonContext)) {
                List<Bitemporal> docs = StreamSupport
                        .stream(queryJournal(journal.getId()).spliterator(), true)
                        .map(d -> fromStoreDocumentPersistenceObject((T) d)).collect(Collectors.toList());
                if (histo.contains(journal.getId()))
                    ((BarbelHistoCore<?>)histo).unloadQuiet(journal.getId());
                ((BarbelHistoCore<?>)histo).loadQuiet(docs);
            }
        } catch (Exception e) {
            event.failed(e);
        }
    }

    public Bitemporal fromStoreDocumentPersistenceObject(T document) {
        String json = fromStoredDocumentToPersistenceObjectJson(document);
        Object object = gson.fromJson(json, persistedType);
        if (object instanceof BitemporalVersion) {
            BitemporalVersion bv = (BitemporalVersion) object;
            bv.setObject(gson.fromJson(gson.toJsonTree(bv.getObject()).toString(), managedType));
        }
        return (Bitemporal)object;
    }

    public abstract String fromStoredDocumentToPersistenceObjectJson(T document);

    @Override
    public abstract Iterable<T> queryAll();

    @Override
    public abstract Iterable<T> queryJournal(Object id);

    @Override
    public abstract R getExternalDataResource();

}
