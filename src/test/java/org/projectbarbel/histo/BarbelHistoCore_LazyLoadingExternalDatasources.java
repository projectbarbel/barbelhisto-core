package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.event.EventType.RetrieveDataEvent;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;

import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.simple.Equal;

public class BarbelHistoCore_LazyLoadingExternalDatasources {

    @BeforeEach
    public void setUp() {
        LazyLoadingListener.externalSource = null;
    }

    @Test
    void shadowCollectionTest() throws Exception {
        // put some sample data into external source
        LazyLoadingListener.externalSource = new HashSet<>();
        LazyLoadingListener.externalSource.add(new DefaultDocument("someId", BitemporalStamp.createActive("someId"), "some data"));
        LazyLoadingListener.externalSource.add(new DefaultDocument("otherId", BitemporalStamp.createActive("otherId"), "some data"));
        LazyLoadingListener.externalSource.add(new DefaultDocument("anotherId", BitemporalStamp.createActive("anotherId"), "some data"));
        LazyLoadingListener.externalSource.add(new DefaultDocument("fooId", BitemporalStamp.createActive("fooId"), "some data"));
        LazyLoadingListener.externalSource.add(new DefaultDocument("barId", BitemporalStamp.createActive("barId"), "some data"));

        // retrieve data from BarbelHisto, which is empty at this point
        BarbelHisto<DefaultDocument> core = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL)
                .withSynchronousEventListener(new LazyLoadingListener()).build();
        List<DefaultDocument> docs = core.retrieve(BarbelQueries.all("someId"));
        
        // will contain the data from lazy loading event handler
        assertEquals(1, docs.size());
        // only someId was loaded
        assertEquals(1, ((BarbelHistoCore<DefaultDocument>)core).size());
        
    }

    @SuppressWarnings("unchecked")
    public static class LazyLoadingListener {
        private static Set<DefaultDocument> externalSource;

        @SuppressWarnings("rawtypes")
        @Subscribe
        public void handleInitialization(RetrieveDataEvent event) {
            Query<DefaultDocument> query = (Query<DefaultDocument>) event.getEventContext()
                    .get(RetrieveDataEvent.QUERY);
            BarbelHisto<DefaultDocument> histo = (BarbelHisto<DefaultDocument>) event.getEventContext()
                    .get(RetrieveDataEvent.BARBEL);
            if (query instanceof Equal) {
                final String id = (String) ((Equal) query).getValue();
                List<Bitemporal> docs = externalSource.stream()
                        .filter(v -> v.getId().equals(id)).collect(Collectors.toList());
                histo.load(docs);
            }
        }

    }
}
