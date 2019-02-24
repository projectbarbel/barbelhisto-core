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
 * 1. {@link EventType#INITIALIZEJOURNAL} when journal is created, only once, abroad post
 * 2. {@link EventType#ACQUIRELOCK}, when {@link BarbelHisto} starts updating a document journal, synchronous post
 * 3. {@link EventType#REPLACEBITEMPORAL}, when old versions are inactivated, abroad post
 * 4. {@link EventType#INSERTBITEMPORAL}, when new versions are inserted, abroad post
 * 5. {@link EventType#RELEASELOCK}, when {@link BarbelHisto} finishes the updating cycle, synchronous post
 * </pre>
 * 
 * The RETRIEVEDATA event is posted when clients retrieve data from
 * {@link BarbelHisto}. "Abroad post" means that the event is published
 * synchronously and asynchronously, i.e. clients can register listeners in
 * asynchronous and synchronous bus to catch these events.<br>
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
 * <br>
 * <br>
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
 * And then register the listeners with
 * {@link BarbelHistoBuilder#withSynchronousEventListener(Object)} or
 * {@link BarbelHistoBuilder#withAsynchronousEventListener(Object)}. <br>
 * <br>
 * If you perform synchronous events, you can control the behavior if the event
 * processing fails. If the handler fails to handle the received synchronous
 * event, then call {@link HistoEvent#failed()}. This will stop execution and an
 * {@link HistoEventFailedException} will be thrown without continuing
 * processing. This could be useful in many situations, e.g. in situations where
 * clients want to avoid any inconsistency between the backbone collection and
 * an external data source targeted by an event. <br>
 * <br>
 * 
 * @author Niklas Schlimm
 *
 */
public enum EventType implements PostableEvent {
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
     * Event fired when {@link BarbelHisto} released a lock on a document journal
     * for a given document ID in the operation. Posted once for each
     * save-operation at the end of the update-operation
     */
    RELEASELOCK {

        @Override
        public HistoEvent create() {
            return new ReleaseLockEvent(RELEASELOCK, new HashMap<>());
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

        public AbstractBarbelEvent(EventType eventType, Map<Object, Object> context) {
            this.eventType = eventType;
            this.eventContext = context;
        }

        public boolean succeeded() {
            return !failed;
        }

        public void failed() {
            failed = true;
        }

        @Override
        public Map<Object, Object> getEventContext() {
            return eventContext;
        }

        public EventType getEventType() {
            return eventType;
        }

    }

    public static class AcquireLockEvent extends AbstractBarbelEvent {

        public AcquireLockEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.SAMPLEJOURNAL).getId();
        }

    }

    public static class InitializeJournalEvent extends AbstractBarbelEvent {

        public InitializeJournalEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.SAMPLEJOURNAL).getId();
        }

    }

    public static class InsertBitemporalEvent extends AbstractBarbelEvent {

        public InsertBitemporalEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.SAMPLEJOURNAL).getId();
        }

    }

    public static class ReleaseLockEvent extends AbstractBarbelEvent {

        public ReleaseLockEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.SAMPLEJOURNAL).getId();
        }

    }

    public static class ReplaceBitemporalEvent extends AbstractBarbelEvent {

        public ReplaceBitemporalEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return Optional.ofNullable(((DocumentJournal) eventContext.get(DocumentJournal.class)))
                    .orElse(DocumentJournal.SAMPLEJOURNAL).getId();
        }

    }

    public static class RetrieveDataEvent extends AbstractBarbelEvent {

        public RetrieveDataEvent(EventType eventType, Map<Object, Object> context) {
            super(eventType, context);
        }

        @Override
        public Object getDocumentId() {
            return "unknown";
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

}
