package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.greaterThan;
import static com.googlecode.cqengine.query.QueryFactory.greaterThanOrEqualTo;
import static com.googlecode.cqengine.query.QueryFactory.lessThan;
import static com.googlecode.cqengine.query.QueryFactory.lessThanOrEqualTo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Iterator;
import java.util.List;

import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.EffectivePeriod;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.logical.LogicalQuery;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.query.simple.SimpleQuery;

/**
 * Convenience methods to perform queries on {@link BarbelHisto#retrieve(Query)}
 * and the like. All queries can be combined with additional CqEngine queries
 * created by {@link QueryFactory}.
 * 
 * @author Niklas Schlimm
 *
 */
public final class BarbelQueries {

    public static final SimpleAttribute<Object, Object> DOCUMENT_ID = new SimpleAttribute<Object, Object>(
            "documentId") {
        public Object getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getDocumentId();
        }
    };

    public static final Attribute<Object, BitemporalObjectState> STATE = new SimpleAttribute<Object, BitemporalObjectState>(
            "state") {
        public BitemporalObjectState getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getRecordTime().getState();
        }
    };

    public static final Attribute<Object, Long> EFFECTIVE_FROM = new SimpleAttribute<Object, Long>(
            "effectiveFrom") {
        public Long getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getEffectiveTime().from().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    };

    public static final Attribute<Object, Long> EFFECTIVE_UNTIL = new SimpleAttribute<Object, Long>(
            "effectiveUntil") {
        public Long getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getEffectiveTime().until().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    };

    public static final Attribute<Object, Long> CREATED_AT = new SimpleAttribute<Object, Long>("createdAt") {
        public Long getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getRecordTime().getCreatedAt()
                    .withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    };

    public static final Attribute<Object, Long> INACTIVATED_AT = new SimpleAttribute<Object, Long>("inactivatedAt") {
        public Long getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getRecordTime().getInactivatedAt()
                    .withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    };

    private BarbelQueries() {
        super();
    }

    //// @formatter:off
    /**
     * Get all versions from the backbone.
     * 
     * @param <T> the POJO type
     * @return all versions stored
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> all() {
        return (Query<T>) QueryFactory.all(Object.class);
    }

    /**
     * Get all versions for one document id.
     * 
     * @param <T> the POJO type
     * @param id the document id
     * @return the versions
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> all(Object id) {
        return (Query<T>) equal(DOCUMENT_ID, id);
    }

    /**
     * Get all active (valid) versions. These will have distinct (non-overlapping)
     * effective periods.
     * 
     * @param <T> the POJO type
     * @param id the document id
     * @return the active versions
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> allActive(Object id) {
        return (Query<T>) and(all(id), equal(STATE, BitemporalObjectState.ACTIVE));
    }

    @SuppressWarnings("rawtypes")
    public static Object returnIDForQuery(Query query) {
        if (query instanceof LogicalQuery) {
            for (Iterator iterator = ((LogicalQuery)query).getChildQueries().iterator(); iterator.hasNext();) {
                return returnIDForQuery((Query)iterator.next());
            }
        }
        if ((query instanceof SimpleQuery)&&(query instanceof Equal)) {
             Equal equal = (Equal) query;
             if (DOCUMENT_ID.equals(equal.getAttribute()))
                 return equal.getValue();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static List<Object> returnIDsForQuery(Query query, List<Object> ids) {
        if (query instanceof LogicalQuery) {
            for (Iterator iterator = ((LogicalQuery)query).getChildQueries().iterator(); iterator.hasNext();) {
                returnIDsForQuery((Query)iterator.next(), ids);
            }
        }
        if ((query instanceof SimpleQuery)&&(query instanceof Equal)) {
             Equal equal = (Equal) query;
             if (DOCUMENT_ID.equals(equal.getAttribute())) {
                 ids.add(equal.getValue());
                 return ids;
            }
        }
        return ids;
    }
    
    /**
     * Get all versions for a document id, that have been inactivated.
     * 
     * @param <T> the POJO type
     * @param id the document id
     * @return the inactivated versions
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> allInactive(Object id) {
        return (Query<T>) and(all(id), equal(STATE, BitemporalObjectState.INACTIVE));
    }

    /**
     * Get the version effective today. Unique object result. Valid query for
     * {@link BarbelHisto#retrieveOne(Query)}.
     * 
     * @param <T> the POJO type
     * @param id the document
     * @return the unique result
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveNow(Object id) {
        return (Query<T>) and(allActive(id),
                lessThanOrEqualTo(EFFECTIVE_FROM, BarbelHistoContext.getBarbelClock().now().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                greaterThan(EFFECTIVE_UNTIL, BarbelHistoContext.getBarbelClock().now().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    /**
     * Get the version effective at given time. Unique object result. Valid query for
     * {@link BarbelHisto#retrieveOne(Query)}.
     * 
     * @param <T> the POJO type
     * @param id  the document
     * @param time effective-at time
     * @return the unique result
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveAt(Object id, ZonedDateTime time) {
        return (Query<T>) and(allActive(id), lessThanOrEqualTo(EFFECTIVE_FROM, time.withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()), greaterThan(EFFECTIVE_UNTIL, time.withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    /**
     * Get the versions effective on or after a given day.
     * 
     * @param <T> the POJO type
     * @param id  the document
     * @param time the effective-after time
     * @return the unique result
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveAfter(Object id, ZonedDateTime time) {
        return (Query<T>) and(allActive(id), greaterThanOrEqualTo(EFFECTIVE_FROM, time.withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    /**
     * Get effective versions in a certain period of time.
     * 
     * @param <T> the POJO type
     * @param id     the dicument id
     * @param period the period
     * @return the list of effective versions
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveBetween(Object id, EffectivePeriod period) {
        if (period.isInfinite())
            return (Query<T>) and(allActive(id), greaterThanOrEqualTo(EFFECTIVE_FROM, period.from().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                    lessThanOrEqualTo(EFFECTIVE_UNTIL, period.until().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        else
            return (Query<T>) and(allActive(id), greaterThanOrEqualTo(EFFECTIVE_FROM, period.from().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                    lessThan(EFFECTIVE_UNTIL, period.until().withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    /**
     * Get the list of active records at a given <b>record</b> time. These will have
     * distinct (non-overlapping) effective periods.
     * 
     * @param <T> the POJO type
     * @param id   the document id
     * @param time the point in time, must be in the past
     * @return the list of versions active at the given time
     */
    @SuppressWarnings("unchecked")
    public static <T> Query<T> journalAt(Object id, ZonedDateTime time) {
        return (Query<T>) and(all(id),
                lessThanOrEqualTo(CREATED_AT,
                        time.toInstant().toEpochMilli()),
                greaterThan(INACTIVATED_AT, time.toInstant().toEpochMilli()));
    }

    // @formatter:on

}
