package org.projectbarbel.histo.event;

import java.util.List;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.model.Bitemporal;

import com.google.common.eventbus.Subscribe;

/**
 * Event fired when {@link BarbelHisto} inserts new version data to a document
 * journal for a given document ID. Can be used to store the new versions
 * elsewhere outside the {@link BarbelHisto} process, e.g. in an external data
 * store. 
 * 
 * @author Niklas Schlimm
 *
 */
public class InsertBitemporalEvent {

    private DocumentJournal documentJournal;
    private List<Bitemporal> newVersions;
    private boolean failed = false;

    public InsertBitemporalEvent(DocumentJournal documentJournal, List<Bitemporal> newVersions) {
        this.documentJournal = documentJournal;
        this.newVersions = newVersions;
    }

    protected List<Bitemporal> getNewVersions() {
        return newVersions;
    }

    protected void setNewVersions(List<Bitemporal> newVersions) {
        this.newVersions = newVersions;
    }

    protected DocumentJournal getDocumentJournal() {
        return documentJournal;
    }

    protected void setDocumentJournal(DocumentJournal documentJournal) {
        this.documentJournal = documentJournal;
    }

    public boolean succeeded() {
        return !failed;
    }

    private void failed() {
        failed = true;
    }

    abstract static class AbstractInsertBitemporalEventListener {
        @Subscribe
        public void handle(InsertBitemporalEvent event) {
            try {
                doHanlde(event);
            } catch (Exception e) {
                event.failed();
            }
        }

        protected abstract void doHanlde(InsertBitemporalEvent event);
    }

}
