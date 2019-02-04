package com.projectbarbel.histo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Supplier;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.joutnal.functions.DefaultIDGenerator;
import com.projectbarbel.histo.model.BitemporalVersion;
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

    static String getDefaultUser() {
        return SYSTEM;
    }
    
    Supplier<?> getDocumentIdGenerator();

    Supplier<?> getVersionIdGenerator();

    IndexedCollection<BitemporalVersion> getBackbone();

    String getActivity();

    String getUser();

    Map<Object, DocumentJournal<BitemporalVersion>> getJournalStore();

}
