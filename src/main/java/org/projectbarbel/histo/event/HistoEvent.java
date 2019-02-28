package org.projectbarbel.histo.event;

import java.util.Map;

import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;

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
        derivedFields(context);
        context.postSynchronousEvent(this);
        if (!this.succeeded())
            throw new HistoEventFailedException("event failed " + this.getClass() + " with ID: " + this.getDocumentId(), this.getRootCause(), this);
    }

    default void postAsynchronous(BarbelHistoContext context) {
        derivedFields(context);
        context.postAsynchronousEvent(this);
    }

    default void postBothWay(BarbelHistoContext context) {
        derivedFields(context);
        postAsynchronous(context);
        postSynchronous(context);
    }

    default void derivedFields(BarbelHistoContext context) {
        setMode(context.getMode());
    }

    void setMode(BarbelMode mode);
    
    boolean succeeded();

    Object getDocumentId();

    void failed(Throwable e);

    Throwable getRootCause();
    
    Map<Object, Object> getEventContext();
    
    EventType getEventType();
}
