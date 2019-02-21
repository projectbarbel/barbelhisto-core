package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.greaterThan;
import static com.googlecode.cqengine.query.QueryFactory.greaterThanOrEqualTo;
import static com.googlecode.cqengine.query.QueryFactory.lessThan;
import static com.googlecode.cqengine.query.QueryFactory.lessThanOrEqualTo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;

import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.EffectivePeriod;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * Convenience methods to perform queries on {@link BarbelHisto#retrieve(Query)}
 * and the like. All queries can be combined with additional cqengine queries
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

    public static final Attribute<Object, ChronoLocalDate> EFFECTIVE_FROM = new SimpleAttribute<Object, ChronoLocalDate>(
            "effectiveFrom") {
        public LocalDate getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getEffectiveTime().from();
        }
    };

    public static final Attribute<Object, ChronoLocalDate> EFFECTIVE_UNTIL = new SimpleAttribute<Object, ChronoLocalDate>(
            "effectiveUntil") {
        public LocalDate getValue(Object object, QueryOptions queryOptions) {
            return ((Bitemporal) object).getBitemporalStamp().getEffectiveTime().until();
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
	 * @return all versions stored
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> all() {
		return (Query<T>) QueryFactory.all(Object.class);
	}

	/**
	 * Get all versions for one document id.
	 * 
	 * @param id the document id
	 * @return the versions
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> all(Object id) {
		return (Query<T>) equal(DOCUMENT_ID, id);
	}

	/**
	 * Get all active (valid) versions. These will have distinct (adjecant,
	 * 'neighboring') effective periods.
	 * 
	 * @param id the document id
	 * @return the active versions
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> allActive(Object id) {
		return (Query<T>) and(all(id), equal(STATE, BitemporalObjectState.ACTIVE));
	}

	/**
	 * Get all inactivated versions for a document id, that have been deactivated.
	 * 
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
	 * @param id the document
	 * @return the unique result
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> effectiveNow(Object id) {
		return (Query<T>) and(allActive(id),
				lessThanOrEqualTo(EFFECTIVE_FROM, BarbelHistoContext.getBarbelClock().now().toLocalDate()),
				greaterThan(EFFECTIVE_UNTIL, BarbelHistoContext.getBarbelClock().now().toLocalDate()));
	}

	/**
	 * Get the version effective at given day. Unique object result. Valid query for
	 * {@link BarbelHisto#retrieveOne(Query)}.
	 * 
	 * @param id the document
	 * @param day effective-at date
	 * @return the unique result
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> effectiveAt(Object id, LocalDate day) {
		return (Query<T>) and(allActive(id), lessThanOrEqualTo(EFFECTIVE_FROM, day), greaterThan(EFFECTIVE_UNTIL, day));
	}

	/**
	 * Get the versions effective on or after a given day. Unique object result.
	 * Valid query for {@link BarbelHisto#retrieveOne(Query)}.
	 * 
	 * @param id the document
	 * @param day the effective-after date
	 * @return the unique result
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> effectiveAfter(Object id, LocalDate day) {
		return (Query<T>) and(allActive(id), greaterThanOrEqualTo(EFFECTIVE_FROM, day));
	}

	/**
	 * Get effective versions in a certain period of time.
	 * 
	 * @param id     the dicument id
	 * @param period the period
	 * @return the list of effective versions
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> effectiveBetween(Object id, EffectivePeriod period) {
	    if (period.until().equals(LocalDate.MAX))
		    return (Query<T>) and(allActive(id), greaterThanOrEqualTo(EFFECTIVE_FROM, period.from()),
		                          lessThanOrEqualTo(EFFECTIVE_UNTIL, period.until()));
	    else 
	        return (Query<T>) and(allActive(id), greaterThanOrEqualTo(EFFECTIVE_FROM, period.from()),
	                              lessThan(EFFECTIVE_UNTIL, period.until()));
	}

	/**
	 * Get the list of active records at a given <b>record</b> time. These will have
	 * distinct (adjecant, 'neighboring') effective periods.
	 * 
	 * @param id   the document id
	 * @param time the point in time, must be in the past
	 * @return the list of versions active at the given time
	 */
	@SuppressWarnings("unchecked")
	public static <T> Query<T> journalAt(Object id, LocalDateTime time) {
		return (Query<T>) and(all(id),
				lessThanOrEqualTo(CREATED_AT,
						ZonedDateTime.of(time, ZoneId.systemDefault()).toInstant().toEpochMilli()),
				greaterThan(INACTIVATED_AT, ZonedDateTime.of(time, ZoneId.systemDefault()).toInstant().toEpochMilli()));
	}

	// @formatter:on

}
