package org.projectbarbel.histo.event;

import java.util.Map;

import org.projectbarbel.histo.BarbelHistoContext;

/**
 * Interface implemented by all {@link EventType}.
 * 
 * @author Niklas Schlimm
 *
 */
public interface HistoEvent {
    default HistoEvent with(Object value) {
        this.getEventContext().put(value.getClass(), value);
        return this;
    }

    default HistoEvent with(Object key, Object value) {
        this.getEventContext().put(key, value);
        return this;
    }

    default void postSynchronous(BarbelHistoContext context) {
        context.postSynchronousEvent(this);
        if (!this.succeeded())
            throw new HistoEventFailedException("event failed " + this.getClass() + " with ID: " + this.getDocumentId(), this.getRootCause(), this);
    }

    default void postAsynchronous(BarbelHistoContext context) {
        context.postAsynchronousEvent(this);
    }

    default void postBothWay(BarbelHistoContext context) {
        postAsynchronous(context);
        postSynchronous(context);
    }

    boolean succeeded();

    Object getDocumentId();

    void failed(Throwable e);

    Throwable getRootCause();
    
    Map<Object, Object> getEventContext();
    
    EventType getEventType();
}
