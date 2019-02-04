package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;

import com.googlecode.cqengine.query.Query;
import com.projectbarbel.histo.model.BitemporalVersion;

public class BarbelQueries {

    public static Query<BitemporalVersion> all(Object id) {
        return equal(BitemporalVersion.DOCUMENT_ID, (String)id);
    }
    
}
