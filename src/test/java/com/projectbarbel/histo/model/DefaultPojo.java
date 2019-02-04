package com.projectbarbel.histo.model;

import com.projectbarbel.histo.DocumentId;

public class DefaultPojo {

    @DocumentId
    private String documentId;
    private String data;
    
    public String getDocumentId() {
        return documentId;
    }
    public String getData() {
        return data;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public void setData(String data) {
        this.data = data;
    }
    
}
