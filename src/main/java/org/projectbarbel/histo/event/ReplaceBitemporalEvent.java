package org.projectbarbel.histo.event;

import java.util.List;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.model.Bitemporal;

import com.google.common.eventbus.Subscribe;

/**
 * Event fired when {@link BarbelHisto} inactivates versions. Can be used to
 * update data sources outside {@link BarbelHisto} in scenarios where data is
 * stored in external data sources.
 * 
 * @author Niklas Schlimm
 *
 */
public class ReplaceBitemporalEvent {

    private List<Bitemporal> objectsToRemove;
    private List<Bitemporal> objectsToAdd;
    private DocumentJournal documentJournal;
    private boolean failed = false;

    public ReplaceBitemporalEvent(DocumentJournal documentJournal, List<Bitemporal> objectsToRemove,
            List<Bitemporal> objectsToAdd) {
        this.setDocumentJournal(documentJournal);
        this.setObjectsToRemove(objectsToRemove);
        this.setObjectsToAdd(objectsToAdd);
    }

    public List<Bitemporal> getObjectsToRemove() {
        return objectsToRemove;
    }

    public void setObjectsToRemove(List<Bitemporal> objectsToRemove) {
        this.objectsToRemove = objectsToRemove;
    }

    public List<Bitemporal> getObjectsToAdd() {
        return objectsToAdd;
    }

    public void setObjectsToAdd(List<Bitemporal> objectsToAdd) {
        this.objectsToAdd = objectsToAdd;
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

    abstract static class AbstractReplaceBitemporalEventListener {
        @Subscribe
        public void handle(ReplaceBitemporalEvent event) {
            try {
                doHanlde(event);
            } catch (Exception e) {
                event.failed();
            }
        }

        protected abstract void doHanlde(ReplaceBitemporalEvent event);
    }

}
