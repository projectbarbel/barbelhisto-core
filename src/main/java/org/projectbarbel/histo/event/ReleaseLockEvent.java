package org.projectbarbel.histo.event;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.DocumentJournal;

import com.google.common.eventbus.Subscribe;

/**
 * Event fired when {@link BarbelHisto} released a lock on a document journal
 * for a given document ID in the
 * {@link BarbelHisto#save(Object, java.time.LocalDate, java.time.LocalDate)}
 * operation. Can be used to release lock on external data sources in scenarios
 * wher multiple {@link BarbelHisto} instances work in separate memory areas but
 * on a single data base.
 * 
 * @author Niklas Schlimm
 *
 */
public class ReleaseLockEvent {

    private DocumentJournal documentJournal;
    private boolean failed = false;

    public ReleaseLockEvent(DocumentJournal documentJournal) {
        this.setDocumentJournal(documentJournal);
    }

    public DocumentJournal getDocumentJournal() {
        return documentJournal;
    }

    public void setDocumentJournal(DocumentJournal documentJournal) {
        this.documentJournal = documentJournal;
    }

    public boolean succeeded() {
        return !failed;
    }

    private void failed() {
        failed = true;
    }

    abstract static class AbstractReleaseLockEventListener {
        @Subscribe
        public void handle(ReleaseLockEvent event) {
            try {
                doHanlde(event);
            } catch (Exception e) {
                event.failed();
            }
        }

        protected abstract void doHanlde(ReleaseLockEvent event);
    }

}
