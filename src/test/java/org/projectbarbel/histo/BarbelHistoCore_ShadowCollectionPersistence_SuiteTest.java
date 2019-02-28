package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.projectbarbel.histo.DocumentJournal.Inactivation;
import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.InitializeJournalEvent;
import org.projectbarbel.histo.event.EventType.RetrieveDataEvent;
import org.projectbarbel.histo.event.EventType.UpdateFinishedEvent;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;

import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;

@TestMethodOrder(OrderAnnotation.class)
public class BarbelHistoCore_ShadowCollectionPersistence_SuiteTest {

    private static IndexedCollection<DefaultDocument> shadow;

    @Order(1)
    @Test
    void shadowExternalTest() throws Exception {
        BarbelHisto<DefaultDocument> barbel = BarbelHistoTestContext.INSTANCE.apply(DefaultDocument.class).withMode(BarbelMode.BITEMPORAL)
                .withSynchronousEventListener(new ShadowCollectionListener()).build();
        DefaultDocument pojo = new DefaultDocument("someId", BitemporalStamp.createActive("someId"), "some data");
        barbel.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, shadow.size());
        pojo.setData("some changes");
        barbel.save(pojo, LocalDate.now().plusDays(2), LocalDate.MAX);
        assertEquals(3, shadow.size());
    }

    @Order(2)
    @Test
    void lazyLoadExternalTest() throws Exception {
        // put some more sample data into external source
        shadow.add(new DefaultDocument("otherId", BitemporalStamp.createActive("otherId"), "some data"));
        shadow.add(new DefaultDocument("anotherId", BitemporalStamp.createActive("anotherId"), "some data"));
        shadow.add(new DefaultDocument("fooId", BitemporalStamp.createActive("fooId"), "some data"));
        shadow.add(new DefaultDocument("barId", BitemporalStamp.createActive("barId"), "some data"));

        // retrieve data from BarbelHisto, which is empty at this point
        BarbelHisto<DefaultDocument> core = BarbelHistoTestContext.INSTANCE.apply(DefaultDocument.class).withMode(BarbelMode.BITEMPORAL)
                .withSynchronousEventListener(new LazyLoadingListener()).build();
        List<DefaultDocument> docs = core.retrieve(BarbelQueries.all("someId"));

        // will contain the data from lazy loading event handler
        assertEquals(3, docs.size());
        // only someId was loaded
        assertEquals(3, ((BarbelHistoCore<DefaultDocument>) core).size());

    }

    public static class ShadowCollectionListener {
        @Subscribe
        public void handleInitialization(BarbelInitializedEvent event) {
            shadow = new ConcurrentIndexedCollection<>();
        }

        @Subscribe
        public void handleInserts(UpdateFinishedEvent event) {
            @SuppressWarnings("unchecked")
            List<Bitemporal> inserts = (List<Bitemporal>) event.getEventContext().get(UpdateFinishedEvent.NEWVERSIONS);
            inserts.stream().forEach(v -> shadow.add((DefaultDocument) v));
            @SuppressWarnings("unchecked")
            Set<Inactivation> inactivations = (Set<Inactivation>) event.getEventContext()
                    .get(UpdateFinishedEvent.INACTIVATIONS);
            inactivations.stream().map(d -> d.getObjectAdded()).forEach(v -> shadow.add((DefaultDocument) v));
            inactivations.stream().map(d -> d.getObjectRemoved()).forEach(v -> shadow.remove((DefaultDocument) v));
        }
    }

    @SuppressWarnings("unchecked")
    public static class LazyLoadingListener {

        @Subscribe
        public void handleRetrieveData(RetrieveDataEvent event) {
            Query<DefaultDocument> query = (Query<DefaultDocument>) event.getEventContext()
                    .get(RetrieveDataEvent.QUERY);
            BarbelHisto<DefaultDocument> histo = (BarbelHisto<DefaultDocument>) event.getEventContext()
                    .get(RetrieveDataEvent.BARBEL);
            final String id = (String) BarbelQueries.returnIDForQuery(query);
            List<Bitemporal> docs = shadow.stream().filter(v -> v.getId().equals(id)).collect(Collectors.toList());
            histo.load(docs);
        }

        @Subscribe
        public void handleInitializeJournal(InitializeJournalEvent event) {
            DocumentJournal journal = (DocumentJournal) event.getEventContext().get(DocumentJournal.class);
            BarbelHisto<DefaultDocument> histo = (BarbelHisto<DefaultDocument>) event.getEventContext()
                    .get(RetrieveDataEvent.BARBEL);
            List<Bitemporal> docs = shadow.stream().filter(v -> v.getId().equals(journal.getId()))
                    .collect(Collectors.toList());
            histo.load(docs);
        }

    }

}
