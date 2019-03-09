package org.projectbarbel.histo.extension;

import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.event.EventType.AcquireLockEvent;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.ReleaseLockEvent;

/**
 * Abstract class to use when implementing custom licking listeners.
 * 
 * @author Niklas Schlimm
 *
 */
public abstract class AbstractLockingListener implements PessimisticLockingProtocol {

    @Override
    public void handleInitialization(BarbelInitializedEvent event) {
        try {
            doInitializeLockCollection();
        } catch (Exception e) {
            event.failed(e);
        }
    }

    @Override
    public void handleAcuireLock(AcquireLockEvent event) {
        try {
            DocumentJournal journal = (DocumentJournal) event.getEventContext().get(DocumentJournal.class);
            doAcquireLock(journal);
        } catch (Exception e) {
            event.failed(e);
        }
    }


    @Override
    public void handleLockRelease(ReleaseLockEvent event) {
        try {
            DocumentJournal journal = (DocumentJournal) event.getEventContext().get(DocumentJournal.class);
            doReleaseLock(journal);
        } catch (Exception e) {
            event.failed(e);
        }
    }
    
    public abstract void doReleaseLock(DocumentJournal journal);

    public abstract void doAcquireLock(DocumentJournal journal);
    
    public abstract void doInitializeLockCollection();

}
