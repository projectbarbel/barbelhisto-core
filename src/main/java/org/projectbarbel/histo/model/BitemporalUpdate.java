package org.projectbarbel.histo.model;

import java.util.List;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;

/**
 * Class that describes the result of a successful invocation of
 * {@link BarbelHisto#save(Object, java.time.ZonedDateTime, java.time.ZonedDateTime)}
 * method.
 * 
 * @author Niklas Schlimm
 *
 * @param <T> the managed type
 */
public class BitemporalUpdate<T> {

    /**
     * The initial update request of the client.
     */
    private final Bitemporal updateRequest;

    /**
     * New versions created by this update.
     */
    private final List<T> inserts;

    /**
     * Existing versions inactivated by this update. The effective periods of these
     * versions have been interrupted by the update request. 
     */
    private final List<T> inactivations;

    /**
     * The update case.
     */
    private final JournalUpdateCase updateCase;

    public BitemporalUpdate(Bitemporal updateRequest, JournalUpdateCase updateCase, List<T> lastInserts,
            List<T> lastInactivations) {
        super();
        this.updateRequest = updateRequest;
        this.updateCase = updateCase;
        this.inserts = lastInserts;
        this.inactivations = lastInactivations;
    }

    public List<T> getInserts() {
        return inserts;
    }

    public List<T> getInactivations() {
        return inactivations;
    }

    public JournalUpdateCase getUpdateCase() {
        return updateCase;
    }

    public Bitemporal getUpdateRequest() {
        return updateRequest;
    }

}
