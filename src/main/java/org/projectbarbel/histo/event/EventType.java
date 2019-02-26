package org.projectbarbel.histo.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.DocumentJournal;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.googlecode.cqengine.query.Query;

import lombok.extern.slf4j.Slf4j;

/**
 * Enum that contains all events thrown by {@link BarbelHisto}. <br>
 * <br>
 * Lifecycle when calling
 * {@link BarbelHisto#save(Object, java.time.LocalDate, java.time.LocalDate)} as
 * follows:
 * 
 * <pre>
 * 1. {@link EventType#BARBELINITIALIZED} when {@link BarbelHisto} instance is created, only once per {@link BarbelHisto} session, both way
 * 2. {@link EventType#INITIALIZEJOURNAL} when journal is created, only once, both way
 * 3. {@link EventType#ACQUIRELOCK}, when {@link BarbelHisto} starts updating a document journal, synchronous post
 * 4. {@link EventType#REPLACEBITEMPORAL}, when versions are inactivated, both way
 * 5. {@link EventType#INSERTBITEMPORAL}, when new versions are inserted, both way
 * 6. {@link EventType#UPDATEFINISHED}, when the update operation for journal was completed, once per save operation, both way
 * 7. {@link EventType#RELEASELOCK}, when {@link BarbelHisto} finishes the updating cycle, synchronous post
 * </pre>
 * 
 * The {@link EventType#RETRIEVEDATA} event is posted each time when clients
 * retrieve data from {@link BarbelHisto}. <br>
 * <br>
 * "Both way" means that the event is published synchronously and
 * asynchronously, i.e. clients can register listeners in asynchronous and
 * synchronous bus to catch these events.<br>
 * <br>
 * Different uses of {@link HistoEvent}s are possible. For instance clients may
 * want to:
 * <ul>
 * <li>synchronize data in external data stores with
 * {@link EventType#REPLACEBITEMPORAL} and
 * {@link EventType#INSERTBITEMPORAL}</li>
 * <li>lock journals stored in a database in complex distributed scenarios using
 * {@link EventType#ACQUIRELOCK} and {@link EventType#RELEASELOCK}</li>
 * <li>lazy load the backbone from external source depending on the data
 * requested by clients using the {@link EventType#RETRIEVEDATA} event</li>
 * </ul>
 * To make use of {@link EventType} create listener classed like so;
 * 
 * <pre>
 * public class MyListener {
 *    <code>@Subscribe</code>
 *    public void handleEvent(AquireLockEvent event) {
 *       // handle the event
 *    }
 * }
 * </pre>
 * 
 * Then register the listeners with
 * {@link BarbelHistoBuilder#withSynchronousEventListener(Object)} or
 * {@link BarbelHistoBuilder#withAsynchronousEventListener(Object)}. <br>
 * <br>
 * If you perform synchronous events, you can control the behavior if the event
 * processing fails. If the handler fails to handle the received synchronous
 * event, then call {@link HistoEvent#failed(Throwable)}. This will stop
 * execution and an {@link HistoEventFailedException} will be thrown without
 * continuing processing. This could be useful in many situations, e.g. in
 * situations where clients want to avoid any inconsistency between the backbone
 * collection and an external data source targeted by an event. <br>
 * <br>
 * 
 * @author Niklas Schlimm
 *
 */
public enum EventType implements PostableEvent {
    /**
     * Event fired when {@link BarbelHisto} is ready for execution.
     */
    BARBELINITIALIZED {
        @Override
        public HistoEvent create() {
            return new BarbelInitializedEvent(BARBELINITIALIZED, new HashMap<>());
        }

    },
    /**
     * Event fired when a journal is created on a
     * {@link BarbelHisto#save(Object, java.time.LocalDate, java.time.LocalDate)}
     * operation. Posted only once per {@link BarbelHisto} session and document id.
     */
    INITIALIZEJOURNAL {

        @Override
        public HistoEvent create() {
            return new InitializeJournalEvent(INITIALIZEJOURNAL, new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} acquires the lock for a journal update.
     * Posted once for each save-operation at the beginning of the update-operation.
     */
    ACQUIRELOCK {
        @Override
        public HistoEvent create() {
            return new AcquireLockEvent(ACQUIRELOCK, new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} inserts new version data to a document
     * journal for a given document ID. Posted once for each save-operation.
     */
    INSERTBITEMPORAL {

        @Override
        public HistoEvent create() {
            return new InsertBitemporalEvent(INSERTBITEMPORAL, new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} inactivates versions. Posted once for
     * each save-operation.
     */
    REPLACEBITEMPORAL {

        @Override
        public HistoEvent create() {
            return new ReplaceBitemporalEvent(REPLACEBITEMPORAL, new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} finished updating the document journal
     * of a given document id. Posted once for each save-operation.
     */
    UPDATEFINISHED {

        @Override
        public HistoEvent create() {
            return new UpdateFinishedEvent(UPDATEFINISHED, new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} released a lock on a document journal
     * for a given document ID in the operation. Posted once for each save-operation
     * at the end of the update-operation
     */
    RELEASELOCK {

        @Override
        public HistoEvent create() {
            return new ReleaseLockEvent(RELEASELOCK, new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} performs a query on request of the
     * client, e.g. in {@link BarbelHisto#retrieve(Query)}. Posted once for each
     * retrieve-operation.
     */
    RETRIEVEDATA {

        @Override
        public HistoEvent create() {
            return new RetrieveDataEvent(RETRIEVEDATA, new HashMap<>());
        }

    };

    public abstract static class AbstractBarbelEvent implements HistoEvent {

        protected Map<Object, Object> eventContext;
        private boolean failed = false;
        private final EventType eventType;
        private Throwable rootCause;

        public Throwable getRootCause() {
            return rootCause;
        }

        public AbstractBarbelEvent(EventType eventType, Map<Object, Object> context) {
            this.eventType = eventType;
            this.eventContext = context;
        }

        public boolean succeeded() {
            return !failed;
        }

        public void failed(Throwable e) {
            this.rootCause = e;
            failed = true;
        }

        @Override
        public Map<Object, Object> getEventContext() {
            return eventContext;
        }

        public EventType getEventType() {
            return eventType;
        }

        public Object getDocumentId() {
            return "n/a";
        }

    }

    public static class AcquireLockEvent extends AbstractBarbelEvent {

        public AcquireLockEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.EMPTYSAMPLE).getId();
        }

    }

    public static class InitializeJournalEvent extends AbstractBarbelEvent {

        public static final String BARBEL = "#core";

        public InitializeJournalEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.EMPTYSAMPLE).getId();
        }

    }

    public static class InsertBitemporalEvent extends AbstractBarbelEvent {

        public static final String NEWVERSIONS = "#newVersions";

        public InsertBitemporalEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.EMPTYSAMPLE).getId();
        }

    }

    public static class ReleaseLockEvent extends AbstractBarbelEvent {

        public ReleaseLockEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.EMPTYSAMPLE).getId();
        }

    }

    public static class ReplaceBitemporalEvent extends AbstractBarbelEvent {

        public static final String OBJECTS_REMOVED = "#objectsRemoved";
        public static final String OBJECTS_ADDED = "#objectsAdded";

        public ReplaceBitemporalEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.EMPTYSAMPLE).getId();
        }

    }

    public static class UpdateFinishedEvent extends AbstractBarbelEvent {
        
        public static final String NEWVERSIONS = "#newVersions";
        public static final String REPLACEMENTS = "#lastreplacements";
        
        public UpdateFinishedEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }
        
        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.EMPTYSAMPLE).getId();
        }
        
    }
    
    public static class RetrieveDataEvent extends AbstractBarbelEvent {

        public static final String QUERY = "#query";
        public static final String BARBEL = "#core";
        public static final String QUERYOPTIONS = "#options";

        public RetrieveDataEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

    }

    @Slf4j
    public static class DefaultSubscriberExceptionHandler implements SubscriberExceptionHandler {

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            log.error("an exception was thrown while executing event handler: "
                    + context.getSubscriberMethod().getDeclaringClass().getName() + "."
                    + context.getSubscriberMethod().getName(), exception);
        }

    }

    public class BarbelInitializedEvent extends AbstractBarbelEvent {

        public BarbelInitializedEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

    }

}
