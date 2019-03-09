package org.projectbarbel.histo.model;

import java.util.List;

import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;

public class BitemporalUpdate<T> {
    
    private final List<T> inserts;
    private final List<T> inactivations;
    private final JournalUpdateCase updateCase;
    private final Bitemporal updateRequest;
    public BitemporalUpdate(Bitemporal updateRequest, JournalUpdateCase updateCase, List<T> lastInserts, List<T> lastInactivations) {
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
