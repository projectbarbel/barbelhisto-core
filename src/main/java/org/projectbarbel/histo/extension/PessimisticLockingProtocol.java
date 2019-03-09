package org.projectbarbel.histo.extension;

import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.event.EventType.AcquireLockEvent;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.ReleaseLockEvent;

import com.google.common.eventbus.Subscribe;

/**
 * Protocol to use when locking {@link DocumentJournal} in multi user scenarios.
 * 
 * @author Niklas Schlimm
 *
 */
public interface PessimisticLockingProtocol {

    @Subscribe
    void handleInitialization(BarbelInitializedEvent event);

    @Subscribe
    void handleAcuireLock(AcquireLockEvent event);

    @Subscribe
    void handleLockRelease(ReleaseLockEvent event);

}
