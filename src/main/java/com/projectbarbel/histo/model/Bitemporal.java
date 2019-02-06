package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * Value Objects in the application must implement this interface.
 * 
 * @author niklasschlimm
 *
 * @param <O> the unique object identifier type of the value object
 */
public interface Bitemporal {

    BitemporalStamp getBitemporalStamp();

    void setBitemporalStamp(BitemporalStamp stamp);

    public static final Attribute<Bitemporal, Object> DOCUMENT_ID = new SimpleAttribute<Bitemporal, Object>("documentId") {
        public Object getValue(Bitemporal object, QueryOptions queryOptions) { return object.getBitemporalStamp().getDocumentId(); }
    };
    
    public static final Attribute<Bitemporal, ChronoLocalDate> EFFECTIVE_FROM = new SimpleAttribute<Bitemporal, ChronoLocalDate>("effectiveFrom") {
        public LocalDate getValue(Bitemporal object, QueryOptions queryOptions) { return object.getBitemporalStamp().getEffectiveTime().from(); }
    };

    public static final Attribute<Bitemporal, ChronoLocalDate> EFFECTIVE_UNTIL = new SimpleAttribute<Bitemporal, ChronoLocalDate>("effectiveUntil") {
        public LocalDate getValue(Bitemporal object, QueryOptions queryOptions) { return object.getBitemporalStamp().getEffectiveTime().until(); }
    };
    
    public static final Attribute<Bitemporal, BitemporalObjectState> STATE = new SimpleAttribute<Bitemporal, BitemporalObjectState>("state") {
        public BitemporalObjectState getValue(Bitemporal object, QueryOptions queryOptions) { return object.getBitemporalStamp().getRecordTime().getState(); }
    };
    
}
