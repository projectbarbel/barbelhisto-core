package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.greaterThan;
import static com.googlecode.cqengine.query.QueryFactory.greaterThanOrEqualTo;
import static com.googlecode.cqengine.query.QueryFactory.lessThanOrEqualTo;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalObjectState;
import com.projectbarbel.histo.model.EffectivePeriod;

public final class BarbelQueries {

    public static final Attribute<Object, Object> DOCUMENT_ID = new SimpleAttribute<Object, Object>("documentId") {
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

    //// @formatter:off    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> all(Object id) {
        return (Query<T>)equal(DOCUMENT_ID, id);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> allActive(Object id) {
        return (Query<T>)and(all(id),
                             equal(STATE, BitemporalObjectState.ACTIVE));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> allInactive(Object id) {
        return (Query<T>)and(all(id),
                             equal(STATE, BitemporalObjectState.INACTIVE));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveNow(Object id) {
        return (Query<T>)and(allActive(id), 
                             lessThanOrEqualTo(EFFECTIVE_FROM, BarbelHistoContext.getDefaultClock().now().toLocalDate()),
                             greaterThan(EFFECTIVE_UNTIL, BarbelHistoContext.getDefaultClock().now().toLocalDate()));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveAt(Object id, LocalDate day) {
        return (Query<T>)and(allActive(id), 
                             lessThanOrEqualTo(EFFECTIVE_FROM, day),
                             greaterThan(EFFECTIVE_UNTIL, day));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveAfter(Object id, LocalDate day) {
        return (Query<T>)and(allActive(id), 
                             greaterThanOrEqualTo(EFFECTIVE_FROM, day));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Query<T> effectiveBetween(Object id, EffectivePeriod period) {
        return (Query<T>)and(allActive(id),
                             greaterThanOrEqualTo(EFFECTIVE_FROM, period.from()),
                             lessThanOrEqualTo(EFFECTIVE_UNTIL, period.until()));
    }
    // @formatter:on

}
