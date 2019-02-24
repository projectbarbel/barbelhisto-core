package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.InsertBitemporalEvent;
import org.projectbarbel.histo.event.EventType.ReplaceBitemporalEvent;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.DefaultPojo;

import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

public class BarbelHistoCore_ShadowCollectionPersistence {

    @BeforeEach
    public void setUp() {
        ShadowCollectionListeners.shadow = null;
    }

    @Test
    void shadowCollectionTest() throws Exception {
        ShadowCollectionListeners.shadow = null;
        BarbelHisto<DefaultPojo> barbel = BarbelHistoBuilder.barbel().withSynchronousEventListener(new ShadowCollectionListeners())
                .build();
        DefaultPojo pojo = new DefaultPojo("someId", "some data");
        barbel.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, ShadowCollectionListeners.shadow.size());
        pojo.setData("some changes");
        barbel.save(pojo, LocalDate.now().plusDays(2), LocalDate.MAX);
        assertEquals(3, ShadowCollectionListeners.shadow.size());
    }

    public static class ShadowCollectionListeners {
        private static IndexedCollection<DefaultPojo> shadow;
        @Subscribe
        public void handleInitialization(BarbelInitializedEvent event) {
            shadow = new ConcurrentIndexedCollection<>();
        }

        @Subscribe
        public void handleInserts(InsertBitemporalEvent event) {
            @SuppressWarnings("unchecked")
            List<Bitemporal> inserts = (List<Bitemporal>) event.getEventContext().get(InsertBitemporalEvent.NEWVERSIONS);
            inserts.stream().forEach(v->shadow.add((DefaultPojo)v));
        }

        @Subscribe
        public void handleReplacements(ReplaceBitemporalEvent event) {
            @SuppressWarnings("unchecked")
            List<Bitemporal> obectsAdded = (List<Bitemporal>) event.getEventContext().get(ReplaceBitemporalEvent.OBJECTS_ADDED);
            @SuppressWarnings("unchecked")
            List<Bitemporal> obectsRemoved = (List<Bitemporal>) event.getEventContext().get(ReplaceBitemporalEvent.OBJECTS_REMOVED);
            obectsAdded.stream().forEach(v->shadow.add((DefaultPojo)v));
            obectsRemoved.stream().forEach(v->shadow.remove((DefaultPojo)v));
        }
    }
}
