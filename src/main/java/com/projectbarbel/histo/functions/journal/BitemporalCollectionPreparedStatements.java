package com.projectbarbel.histo.functions.journal;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.resultset.ResultSet;
import com.projectbarbel.histo.model.Bitemporal;

public class BitemporalCollectionPreparedStatements {

    @SuppressWarnings("unchecked")
    public static <T extends Bitemporal<?>> ResultSet<T> getByID_orderByEffectiveFrom(IndexedCollection<T> documents, String id) {
        return documents.retrieve(equal((Attribute<T, String>) Bitemporal.DOCUMENT_ID, (String) id),
                queryOptions(orderBy(ascending(Bitemporal.EFFECTIVE_FROM))));
    }
    
}
