package com.projectbarbel.histo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.functions.CGIPojoProxyingFunction;
import com.projectbarbel.histo.journal.functions.DefaultIDGenerator;
import com.projectbarbel.histo.journal.functions.GsonPojoCopier;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.Systemclock;

public interface BarbelHistoContext<T> {

    public static final String SYSTEM = "SYSTEM";
    public static final String SYSTEMACTIVITY = "SYSTEMACTIVITY";
    public static final Systemclock CLOCK = new Systemclock();
    
    static String getDefaultActivity() {
        return SYSTEMACTIVITY;
    }
    
    static LocalDate getInfiniteDate() {
        return LocalDate.MAX;
    }
    
    static Systemclock getClock() {
        return CLOCK;
    }

    static Supplier<Serializable> getDefaultDocumentIDGenerator() {
        return new DefaultIDGenerator();
    }

    static Supplier<Serializable> getDefaultVersionIDGenerator() {
        return new DefaultIDGenerator();
    }

    static String getDefaultUser() {
        return SYSTEM;
    }
    
    static <T> BiFunction<T, BitemporalStamp, T> getDefaultProxyingFunction() {
        return new CGIPojoProxyingFunction<T>();
    }

    static Gson getDefaultGson() {
        return new Gson();
    }

    static <T> Function<T, T> getDefaultCopyFunction() {
        return new GsonPojoCopier<T>();
    }

    Supplier<?> getDocumentIdGenerator();

    Supplier<?> getVersionIdGenerator();

    IndexedCollection<T> getBackbone();

    String getActivity();

    String getUser();

    Map<Object, DocumentJournal<? extends Bitemporal<?>>> getJournalStore();

    BiFunction<T, BitemporalStamp, T> getPojoProxyingFunction();

    Function<T, T> getPojoCopyFunction();

}
