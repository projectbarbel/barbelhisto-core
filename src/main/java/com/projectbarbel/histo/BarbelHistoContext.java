package com.projectbarbel.histo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.function.Supplier;

import com.projectbarbel.histo.functions.DefaultIDGenerator;
import com.projectbarbel.histo.model.Systemclock;

public interface BarbelHistoContext {

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

    static String getDefaultCreatedBy() {
        return SYSTEM;
    }
    
    Supplier<String> getDocumentIdGenerator();

    Supplier<?> getVersionIdGenerator();

}
