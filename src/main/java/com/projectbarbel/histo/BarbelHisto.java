package com.projectbarbel.histo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalVersion;

public interface BarbelHisto {

    boolean save(Object currentVersion, LocalDate from, LocalDate until);

    <T> List<T> retrieve(Query<Object> query);

    <T> List<T> retrieve(Query<Object> query, QueryOptions options);

    String prettyPrintJournal(Object id);

    /**
     * Method for clients that use a custom data store. It's recomended to only add
     * complete journals previously created by {@link BarbelHisto}. That means only
     * add complete set of versions for one or more document IDs. To achieve this,
     * preferably use this method with version collections produced by
     * {@link #dump()} method. That ensures consistent state with intermediate
     * persistence operations to custom data stores. You can also use
     * {@link BarbelQueries#all(Object)} to retrieve a complete Journal for a single
     * document id, or use {@link BarbelQueries#all()} to retrieve the complete data
     * in one Barbel Histo backbone. However, notice that {@link #retrieve(Query)}
     * does not clear the backbone. If you try to {@link #populate(Collection)}
     * items already managed in the current {@link BarbelHisto} instance you will
     * receive errors.
     * 
     * Notice that this way of persistence to a custom data store is not the
     * recommended way for BarbelHosto persistence. The cqengine backbone several
     * build in on-heap (default), off-heap and disk persistence options. See
     * {@link DiskPersistence} and {@link OffHeapPersistence} for details.
     * 
     * In Pojo mode (default) you have to pass a collection of {@link BitemporalVersion}
     * objects. In Bitemporal mode you can ad the {@link Bitemporal} objects.
     * 
     * @see <a href=
     *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
     * @param bitemporals consistent list of bitemporal versions
     */
    void populate(Collection<Bitemporal> bitemporals);

    /**
     * Unloads the backbone into a collection and return that to the client. This
     * method is recommended when client uses custom data store. Store this
     * collection to the data store of your choice. Use in conjunction with
     * {@link #populate(Collection)} to re-load the stored versions back into
     * {@link BarbelHisto}.
     * 
     * In Pojo mode (default) this method returns a collection of
     * {@link BitemporalVersion} objects. Use this collection to store the data and
     * to relaod {@link BarbelHisto} in the {@link #populate(Collection)} method.
     * 
     * @return the collection of bitemporals to store into an arbitrary data store
     */
    Collection<Bitemporal> dump();

}
