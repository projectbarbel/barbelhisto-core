package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.model.Bitemporal;

public final class BarbelQueries {

    public static final Attribute<Object, Object> DOCUMENT_ID = new SimpleAttribute<Object, Object>("documentId") {
        public Object getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getDocumentId();
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Query<T> all(Object id) {
        return (Query<T>) equal(DOCUMENT_ID, (String) id);
    }

}
