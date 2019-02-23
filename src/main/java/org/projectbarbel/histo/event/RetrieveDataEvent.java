package org.projectbarbel.histo.event;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;

import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.query.Query;

/**
 * Event fired when {@link BarbelHisto} performs a query on request of the
 * client, e.g. in {@link BarbelHisto#retrieve(Query)}. Can be used to fetch
 * data for that given query from an external data source in order to satisfy
 * the client query (lazy loading the backbone from external data source).
 * 
 * @author Niklas Schlimm
 *
 */
public class RetrieveDataEvent {
    private BarbelHistoContext context;
    private Query<?> query;
    private boolean failed = false;

    public RetrieveDataEvent(BarbelHistoContext context, Query<?> query) {
        this.setContext(context);
        this.setQuery(query);
    }

    public BarbelHistoContext getContext() {
        return context;
    }

    public void setContext(BarbelHistoContext context) {
        this.context = context;
    }

    public Query<?> getQuery() {
        return query;
    }

    public void setQuery(Query<?> query) {
        this.query = query;
    }

    public boolean succeeded() {
        return !failed;
    }

    private void failed() {
        failed = true;
    }

    abstract static class AbstractRetrieveDataEventListener {
        @Subscribe
        public void handle(RetrieveDataEvent event) {
            try {
                doHanlde(event);
            } catch (Exception e) {
                event.failed();
            }
        }

        protected abstract void doHanlde(RetrieveDataEvent event);
    }
}
