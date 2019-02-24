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
        this.getEventContext().put(value.getClass().getName(), value);
        return this;
    }

    default HistoEvent with(Object key, Object value) {
        this.getEventContext().put(key, value);
        return this;
    }

    default void postSynchronous(BarbelHistoContext context) {
        context.postSynchronousEvent(this);
        if (!this.succeeded())
            throw new HistoEventFailedException("event failed " + this.getClass() + " with ID: " + this.getDocumentId(), this);
    }

    default void postAsynchronous(BarbelHistoContext context) {
        context.postAsynchronousEvent(this);
    }

    default void postAbroad(BarbelHistoContext context) {
        postAsynchronous(context);
        postSynchronous(context);
    }

    boolean succeeded();

    Object getDocumentId();

    void failed();

    Map<Object, Object> getEventContext();
    
    EventType getEventType();
}
