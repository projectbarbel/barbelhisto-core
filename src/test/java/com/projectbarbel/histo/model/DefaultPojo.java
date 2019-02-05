package com.projectbarbel.histo.model;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.DocumentId;

public class DefaultPojo {

    public static final Attribute<DefaultPojo, String> DOCUMENT_ID = new SimpleAttribute<DefaultPojo, String>("documentId") {
        public String getValue(DefaultPojo object, QueryOptions queryOptions) { return object.getDocumentId(); }
    };

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
