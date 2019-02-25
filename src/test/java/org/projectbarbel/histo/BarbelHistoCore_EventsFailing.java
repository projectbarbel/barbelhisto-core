package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.InsertBitemporalEvent;
import org.projectbarbel.histo.event.EventType.ReplaceBitemporalEvent;
import org.projectbarbel.histo.event.HistoEventFailedException;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;

import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

@TestMethodOrder(OrderAnnotation.class)
public class BarbelHistoCore_EventsFailing {

    private static IndexedCollection<DefaultDocument> shadow;
    private static int replaceCounter = 0;

    @Order(1)
    @Test
    void shadowExternalTest() throws Exception {
        BarbelHisto<DefaultDocument> barbel = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL)
                .withSynchronousEventListener(new ShadowCollectionListeners()).build();
        DefaultDocument pojo = new DefaultDocument("someId", BitemporalStamp.createActive("someId"), "some data");
        barbel.save(pojo, LocalDate.now(), LocalDate.MAX);
        pojo.setData("change some");
        assertThrows(HistoEventFailedException.class,
                () -> barbel.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX));
        // performed one replace
        assertEquals(1, replaceCounter);
        // update was processed completely in mirror
        assertEquals(3, shadow.size());
        // inactivation (replace) was processed in mirror
        assertEquals(1, shadow.stream().filter(p->!p.getBitemporalStamp().isActive()).count()); 
        // histo core rolled back
        assertEquals(1, ((BarbelHistoCore<DefaultDocument>) barbel).size());
        assertTrue(barbel.retrieveOne(BarbelQueries.effectiveAt("someId", LocalDate.now().plusDays(2))).getData()
                .equals("some data"));
    }

    public static class ShadowCollectionListeners {
        @Subscribe
        public void handleInitialization(BarbelInitializedEvent event) {
            shadow = new ConcurrentIndexedCollection<>();
        }

        @Subscribe
        public void handleInserts(InsertBitemporalEvent event) {
            try {
                @SuppressWarnings("unchecked")
                List<Bitemporal> inserts = (List<Bitemporal>) event.getEventContext()
                        .get(InsertBitemporalEvent.NEWVERSIONS);
                inserts.stream().forEach(v -> shadow.add((DefaultDocument) v));
                if (shadow.size() > 1)
                    throw new NullPointerException();
            } catch (Exception e) {
                event.failed();
            }
        }

        @Subscribe
        public void handleReplacements(ReplaceBitemporalEvent event) {
            // Fail when one record was already inserted
            try {
                replaceCounter++;
                @SuppressWarnings("unchecked")
                List<Bitemporal> obectsAdded = (List<Bitemporal>) event.getEventContext()
                        .get(ReplaceBitemporalEvent.OBJECTS_ADDED);
                @SuppressWarnings("unchecked")
                List<Bitemporal> obectsRemoved = (List<Bitemporal>) event.getEventContext()
                        .get(ReplaceBitemporalEvent.OBJECTS_REMOVED);
                obectsAdded.stream().forEach(v -> shadow.add((DefaultDocument) v));
                obectsRemoved.stream().forEach(v -> shadow.remove((DefaultDocument) v));
            } catch (Exception e) {
                event.failed();
            }
        }
    }

}
