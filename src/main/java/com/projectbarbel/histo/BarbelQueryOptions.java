package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import com.googlecode.cqengine.query.option.QueryOptions;

public class BarbelQueryOptions {

    public static QueryOptions sortAscendingByEffectiveFrom() {
        return queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM), ascending(BarbelQueries.EFFECTIVE_UNTIL)));
    }

}
