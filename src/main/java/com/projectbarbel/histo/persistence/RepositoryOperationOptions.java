package com.projectbarbel.histo.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RepositoryOperationOptions {
    
    private List<Object> journalIDs = new ArrayList<Object>();
    private Map<String, String> customLoadOptions;

    public Map<String, String> getCustomLoadOptions() {
        return customLoadOptions;
    }

    public void addCustomOption(String key, String value) {
        customLoadOptions.put(key, value);
    }
    
    public List<Object> getJournalIDs() {
        return journalIDs;
    }

    public void addJournalIDs(Object... journalIDs) {
        this.journalIDs.addAll(Arrays.asList(journalIDs));
    }
    
    public void addJournalIDs(List<Object> journalIDs) {
        this.journalIDs.addAll(journalIDs);
    }

}
