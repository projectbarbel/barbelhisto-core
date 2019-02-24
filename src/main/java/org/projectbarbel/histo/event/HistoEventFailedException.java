package org.projectbarbel.histo.event;

@SuppressWarnings("serial")
public class HistoEventFailedException extends RuntimeException {

    private final Class<?> eventType;
    
    public HistoEventFailedException(String message, Throwable cause, Class<?> eventType) {
        super(message, cause);
        this.eventType = eventType;
    }

    public HistoEventFailedException(String message, Class<?> eventType) {
        super(message);
        this.eventType = eventType;
    }
    
    protected Class<?> getEventType() {
        return eventType;
    }

}


