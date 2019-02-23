package org.projectbarbel.histo.event;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.DocumentJournal;

import com.google.common.eventbus.Subscribe;

/**
 * Event fired when a journal is created on a
 * {@link BarbelHisto#save(Object, java.time.LocalDate, java.time.LocalDate)}
 * operation. Can be used to dynamically load data from backends, e.g. if
 * document journals are stored in external data sources. You can then
 * initialize the journal for the given document ID with the existing version
 * data before {@link BarbelHisto} actually processes the update for the new
 * version passed to the save operation.
 * 
 * @author Niklas Schlimm
 *
 */
public class InitializeJournalEvent {
    private DocumentJournal journal;
    private boolean failed = false;

    public DocumentJournal getDocumentJournal() {
        return journal;
    }

    public InitializeJournalEvent(DocumentJournal journal) {
        this.journal = journal;
    }

    public boolean succeeded() {
        return !failed;
    }

    private void failed() {
        failed = true;
    }

    abstract static class AbstractInitializeJournalEventListener {
        @Subscribe
        public void handle(InitializeJournalEvent event) {
            try {
                doHanlde(event);
            } catch (Exception e) {
                event.failed();
            }
        }

        protected abstract void doHanlde(InitializeJournalEvent event);
    }

}
