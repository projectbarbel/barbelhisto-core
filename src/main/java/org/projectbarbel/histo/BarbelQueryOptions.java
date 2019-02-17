package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * Convenience class to pass options to
 * {@link BarbelHisto#retrieve(com.googlecode.cqengine.query.Query, QueryOptions)}.
 * Clients can subclass this class to create additional options.
 * 
 * @author Niklas Schlimm
 *
 */
public class BarbelQueryOptions {

    public static QueryOptions sortAscendingByEffectiveFrom() {
        return queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM), ascending(BarbelQueries.EFFECTIVE_UNTIL)));
    }

}
