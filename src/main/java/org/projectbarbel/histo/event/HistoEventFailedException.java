package org.projectbarbel.histo.event;

/**
 * Exception thrown when synchronous {@link HistoEvent} fails. Provides access
 * to the failing event to enable specific error handling by the client. Only
 * thrown when {@link HistoEvent#failed(Throwable)} is called in the listener
 * implementation. Clients can pass back context data to their exception
 * handling by adding data to {@link HistoEvent#getEventContext()}.
 * 
 * @author Niklas Schlimm
 *
 */
@SuppressWarnings("serial")
public class HistoEventFailedException extends RuntimeException {

    private final transient HistoEvent event;

    public HistoEventFailedException(String message, Throwable cause, HistoEvent event) {
        super(message, cause);
        this.event = event;
    }

    public HistoEventFailedException(String message, HistoEvent event) {
        super(message);
        this.event = event;
    }

    public HistoEvent getEvent() {
        return event;
    }

}
