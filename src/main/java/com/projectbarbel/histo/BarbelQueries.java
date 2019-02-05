package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.model.Bitemporal;

public class BarbelQueries {

    public static final Attribute<Object, String> DOCUMENT_ID = new SimpleAttribute<Object, String>("documentId") {
        public String getValue(Object object, QueryOptions queryOptions) { return ((Bitemporal<?>)object).getDocumentId(); }
    };
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> all(Object id) {
        return (Query<T>) equal(DOCUMENT_ID, (String)id);
    }
    
}
