package com.projectbarbel.histo.joutnal.functions;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.greaterThan;
import static com.googlecode.cqengine.query.QueryFactory.greaterThanOrEqualTo;
import static com.googlecode.cqengine.query.QueryFactory.lessThanOrEqualTo;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.resultset.ResultSet;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalObjectState;
import com.projectbarbel.histo.model.EffectivePeriod;

public class BitemporalCollectionPreparedStatements {

    //// @formatter:off
    @SuppressWarnings("unchecked")
    public static <T extends Bitemporal<?>> ResultSet<T> getAll_ByID_orderByEffectiveFrom(IndexedCollection<T> documents, String id) {
        return documents.retrieve(equal((Attribute<T, String>) Bitemporal.DOCUMENT_ID, (String) id),
                                  queryOptions(orderBy(ascending(Bitemporal.EFFECTIVE_FROM))));
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Bitemporal<?>> ResultSet<T> getActiveVersionsEffectiveAfter_ByDate_orderByEffectiveFrom(IndexedCollection<T> documents, LocalDate date) {
        return documents.retrieve(and(greaterThanOrEqualTo((Attribute<T, ChronoLocalDate>) Bitemporal.EFFECTIVE_FROM, date),
                                      equal((Attribute<T, BitemporalObjectState>) Bitemporal.STATE, BitemporalObjectState.ACTIVE)),
                                  queryOptions(orderBy(ascending(Bitemporal.EFFECTIVE_FROM))));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Bitemporal<?>> ResultSet<T> getActiveVersionsEffectiveBetween_ByFromAndUntilDate_orderByEffectiveFrom(IndexedCollection<T> documents, EffectivePeriod period) {
        return documents.retrieve(and(greaterThanOrEqualTo((Attribute<T, ChronoLocalDate>) Bitemporal.EFFECTIVE_FROM, period.from()),
                                      lessThanOrEqualTo((Attribute<T, ChronoLocalDate>) Bitemporal.EFFECTIVE_UNTIL, period.until()),
                                      equal((Attribute<T, BitemporalObjectState>) Bitemporal.STATE, BitemporalObjectState.ACTIVE)),
                                  queryOptions(orderBy(ascending(Bitemporal.EFFECTIVE_FROM))));
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Bitemporal<?>> ResultSet<T> getActiveVersionEffectiveOn_ByDate(IndexedCollection<T> documents, LocalDate date) {
        ResultSet<T> set = documents.retrieve(and(lessThanOrEqualTo((Attribute<T, ChronoLocalDate>) Bitemporal.EFFECTIVE_FROM, date),
                                                  greaterThan((Attribute<T, ChronoLocalDate>) Bitemporal.EFFECTIVE_UNTIL, date),
                                                  equal((Attribute<T, BitemporalObjectState>) Bitemporal.STATE, BitemporalObjectState.ACTIVE)));
        return set;
    }
    // @formatter:on

}
