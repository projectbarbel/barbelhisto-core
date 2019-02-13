package com.projectbarbel.histo.model;

import java.util.Objects;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.DocumentId;

public class DefaultPojo {

    public static final Attribute<DefaultPojo, String> DOCUMENT_ID = new SimpleAttribute<DefaultPojo, String>("documentId") {
        public String getValue(DefaultPojo object, QueryOptions queryOptions) { return object.getDocumentId(); }
    };

    public DefaultPojo(String documentId, String data) {
        super();
        this.documentId = documentId;
        this.data = data;
    }

    public DefaultPojo() {
        super();
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(data, documentId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultPojo)) {
            return false;
        }
        DefaultPojo other = (DefaultPojo) obj;
        return Objects.equals(data, other.data) && Objects.equals(documentId, other.documentId);
    }

    @Override
    public String toString() {
        return "DefaultPojo [documentId=" + documentId + ", data=" + data + "]";
    }
    
}
