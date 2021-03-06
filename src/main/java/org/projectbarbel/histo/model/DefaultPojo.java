package org.projectbarbel.histo.model;

import java.util.Objects;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.query.option.QueryOptions;

@PersistenceConfig(serializer = BarbelPojoSerializer.class, polymorphic = true)
public class DefaultPojo {

	public static final SimpleAttribute<DefaultPojo, String> DOCUMENT_ID = new SimpleAttribute<DefaultPojo, String>("documentId") {
		public String getValue(DefaultPojo object, QueryOptions queryOptions) { return object.getDocumentId(); }
	};
	
    @DocumentId
    private String documentId;
    private String data;
    
    public DefaultPojo(String documentId, String data) {
        super();
        this.documentId = documentId;
        this.data = data;
    }

    public DefaultPojo() {
        super();
    }

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
