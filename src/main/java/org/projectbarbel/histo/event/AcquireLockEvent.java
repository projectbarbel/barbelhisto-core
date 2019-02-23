package org.projectbarbel.histo.event;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.DocumentJournal;

import com.google.common.eventbus.Subscribe;

/**
 * Event fired when {@link BarbelHisto} acquires the lock for a journal update.
 * Use this to lock the journal elsewhere, e.g. in a custom data store to manage
 * transactions across multiple instances of {@link BarbelHisto} livin in
 * separate memory areas. 
 * 
 * @author Niklas Schlimm
 *
 */
public class AcquireLockEvent {

    private final DocumentJournal documentJournal;
    private boolean failed = false;

    public AcquireLockEvent(DocumentJournal documentJournal) {
        this.documentJournal = documentJournal;
    }

    public DocumentJournal getDocumentJournal() {
        return documentJournal;
    }

    public boolean succeeded() {
        return !failed;
    }

    private void failed() {
        failed = true;
    }

    abstract static class AbstractAcquireLockListener {
        @Subscribe
        public void handle(AcquireLockEvent event) {
            try {
                doHanlde(event);
            } catch (Exception e) {
                event.failed();
            }
        }

        protected abstract void doHanlde(AcquireLockEvent event);
    }

}
