package org.projectbarbel.histo.event;

import java.util.HashMap;
import java.util.Map;

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
 * 1. {@link Events#INITIALIZEJOURNAL} when journal is created, only once, abroad post
 * 2. {@link Events#ACQUIRELOCK}, when {@link BarbelHisto} starts updating a document journal, synchronous post
 * 3. {@link Events#REPLACEBITEMPORAL}, when old versions are inactivated, abroad post
 * 4. {@link Events#INSERTBITEMPORAL}, when new versions are inserted, abroad post
 * 5. {@link Events#RELEASELOCK}, when {@link BarbelHisto} finishes the updating cycle, synchronous post
 * </pre>
 * 
 * The RETRIEVEDATA event is posted when clients retrieve data from
 * {@link BarbelHisto}. "Abroad post" means that the event is published
 * synchronously and asynchronously, i.e. clients can register listeners in
 * asynchronous and synchronous bus to catch these events.<br>
 * <br>
 * Different uses of {@link HistoEvent}s are possible. For instance clients may
 * want to:
 * <li>synchronize data in external data stores with
 * {@link Events#REPLACEBITEMPORAL} and {@link Events#INSERTBITEMPORAL}</li>
 * <li>lock journals stored in a database in complex distributed scenarios using
 * {@link Events#ACQUIRELOCK} and {@link Events#RELEASELOCK}</li>
 * <li>lazy load the backbone from external source depending on the data
 * requested by clients using the {@link Events#RETRIEVEDATA} event</li> <br>
 * <br>
 * To make use of {@link Events} create listener classed like so;
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
 * And the register these listeners with
 * {@link BarbelHistoBuilder#withSynchronousEventListener(Object)} or
 * {@link BarbelHistoBuilder#withAsynchronousEventListener(Object)}. <br>
 * <br>
 * If you perform synchronous events, you can control the behavior if the event
 * processing fails. If the handler fails to handle the received synchronous
 * event, then call {@link HistoEvent#failed()}. This will stop execution and an
 * {@link HistoEventFailedException} will be thrown without continuing
 * processing. This might be useful, e.g. in situations where clients want to
 * avoid any inconsistency between the backbone collection and an external data
 * source targeted by an event. <br>
 * <br>
 * 
 * @author Niklas Schlimm
 *
 */
public enum Events implements PostableEvent {
    /**
     * Event fired when {@link BarbelHisto} acquires the lock for a journal update.
     */
    ACQUIRELOCK {
        @Override
        public HistoEvent create() {
            return new AcquireLockEvent(new HashMap<>());
        }

    },
    /**
     * Event fired when a journal is created on a
     * {@link BarbelHisto#save(Object, java.time.LocalDate, java.time.LocalDate)}
     * operation.
     */
    INITIALIZEJOURNAL {

        @Override
        public HistoEvent create() {
            return new InitializeJournalEvent(new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} inserts new version data to a document
     * journal for a given document ID.
     */
    INSERTBITEMPORAL {

        @Override
        public HistoEvent create() {
            return new InsertBitemporalEvent(new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} released a lock on a document journal
     * for a given document ID in the operation.
     */
    RELEASELOCK {

        @Override
        public HistoEvent create() {
            return new ReleaseLockEvent(new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} inactivates versions.
     */
    REPLACEBITEMPORAL {

        @Override
        public HistoEvent create() {
            return new ReplaceBitemporalEvent(new HashMap<>());
        }

    },
    /**
     * Event fired when {@link BarbelHisto} performs a query on request of the
     * client, e.g. in {@link BarbelHisto#retrieve(Query)}.
     */
    RETRIEVEDATA {

        @Override
        public HistoEvent create() {
            return new RetrieveDataEvent(new HashMap<>());
        }

    };

    public abstract static class AbstractBarbelEvent implements HistoEvent {

        protected Map<Object, Object> eventContext;
        protected boolean failed = false;

        public AbstractBarbelEvent(Map<Object, Object> context) {
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

    }

    public static class AcquireLockEvent extends AbstractBarbelEvent {

        public AcquireLockEvent(Map<Object, Object> context) {
            super(context);
        }

        @Override
        public Object getDocumentId() {
            return ((DocumentJournal) eventContext.get(DocumentJournal.class)).getId();
        }

    }

    public static class InitializeJournalEvent extends AbstractBarbelEvent {

        public InitializeJournalEvent(Map<Object, Object> eventContext) {
            super(eventContext);
        }

        @Override
        public Object getDocumentId() {
            return ((DocumentJournal) eventContext.get(DocumentJournal.class)).getId();
        }

    }

    public static class InsertBitemporalEvent extends AbstractBarbelEvent {

        public InsertBitemporalEvent(Map<Object, Object> eventContext) {
            super(eventContext);
        }

        @Override
        public Object getDocumentId() {
            return ((DocumentJournal) eventContext.get(DocumentJournal.class)).getId();
        }

    }

    public static class ReleaseLockEvent extends AbstractBarbelEvent {

        public ReleaseLockEvent(Map<Object, Object> eventContext) {
            super(eventContext);
        }

        @Override
        public Object getDocumentId() {
            return ((DocumentJournal) eventContext.get(DocumentJournal.class)).getId();
        }

    }

    public static class ReplaceBitemporalEvent extends AbstractBarbelEvent {

        public ReplaceBitemporalEvent(Map<Object, Object> eventContext) {
            super(eventContext);
        }

        @Override
        public Object getDocumentId() {
            return ((DocumentJournal) eventContext.get(DocumentJournal.class)).getId();
        }

    }

    public static class RetrieveDataEvent extends AbstractBarbelEvent {

        public RetrieveDataEvent(Map<Object, Object> eventContext) {
            super(eventContext);
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
