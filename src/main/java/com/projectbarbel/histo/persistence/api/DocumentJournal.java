package com.projectbarbel.histo.persistence.api;

import java.util.ArrayList;
import java.util.List;

public class DocumentJournal {
    
    private List<?> journal = new ArrayList<>();

    public void setJournal(List<?> journal) {
        this.journal = journal;
    }


    public List<?> getJournal() {
        return journal;
    }
    
}