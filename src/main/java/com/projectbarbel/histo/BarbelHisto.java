package com.projectbarbel.histo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.BarbelHistoCore.DumpMode;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;
import com.projectbarbel.histo.model.DocumentJournal;

/**
 * Two time dimensions
 * 
 * Two {@link BarbelMode}s
 * 
 * 
 * The wording:
 * 
 * - a 'managed bitemporal' is either a proxied pojo or an object implementing {@link Bitemporal}, managed objects are the backbone citizens
 * - 'bitemporal objects' are objects implementing the {@link Bitemporal} interface, as long they don't live in the backbone, they're not considered managed bitemporals
 * - a snapshot always creates a NEW managed bitemporal with a new given {@link BitemporalStamp}
 * - a custom persistent object is always bitemporal object, but not managed; in {@link BarbelMode.PojoMode} it is always a {@link BitemporalVersion}
 * 
 * @author niklasschlimm
 *
 * @param <T>
 */
public interface BarbelHisto<T> {

    boolean save(T currentVersion, LocalDate from, LocalDate until);

    List<T> retrieve(Query<T> query);

    List<T> retrieve(Query<T> query, QueryOptions options);

    Optional<T> retrieveOne(Query<T> query);
    
    Optional<T> retrieveOne(Query<T> query, QueryOptions options);
    
    DocumentJournal timeshift(Object id, LocalDateTime time);
    
    String prettyPrintJournal(Object id);
    
    /**
     * Method for clients that use a custom data store. It's recomended to only add
     * complete journals previously created by {@link BarbelHisto}. That means only
     * add complete set of versions for one or more document IDs. To achieve this,
     * preferably use this method with version collections produced by
     * {@link #dump(DumpMode)} method. That ensures consistent state with intermediate
     * persistence operations to custom data stores. You can also use
     * {@link BarbelQueries#all(Object)} to retrieve a complete Journal for a single
     * document id, or use {@link BarbelQueries#all()} to retrieve the complete data
     * in one Barbel Histo backbone. However, notice that {@link #retrieve(Query)}
     * does not clear the backbone. If you try to {@link #populate(Collection)}
     * items already managed in the current {@link BarbelHisto} instance you will
     * receive errors. <br>
     * <br>
     * In {@link BarbelMode#POJO} (default) you have to pass a collection of
     * {@link BitemporalVersion} objects. In {@link BarbelMode#BITEMPORAL} mode you
     * can add arbitrary objects that implement {@link Bitemporal}. <br>
     * <br>
     * Notice that this way of persistence to a custom data store is not the
     * recommended way for {@link BarbelHisto} persistence. {@link BarbelHisto} is
     * based on cqengine. There are build in on-heap (default), off-heap and disk
     * persistence options. See {@link DiskPersistence} and
     * {@link OffHeapPersistence} for details. <br>
     * <br>
     * 
     * @see <a href=
     *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
     * @param bitemporals consistent list of bitemporal versions
     */
    void populate(Collection<Bitemporal> bitemporals);

    /**
     * Unloads the backbone into a collection and return that to the client. This
     * method is used when client uses custom data store. Store this collection to
     * the data store of your choice. Use in conjunction with
     * {@link #populate(Collection)} to re-load the stored versions back into
     * {@link BarbelHisto}.<br>
     * <br>
     * In {@link BarbelMode#POJO} (default) you receive a collection of
     * {@link BitemporalVersion} objects. In {@link BarbelMode#BITEMPORAL} you
     * receive objects that implement {@link Bitemporal}. <br>
     * <br>
     * In Pojo mode (default) this method returns a collection of
     * {@link BitemporalVersion} objects. Use this collection to store the data and
     * to relaod {@link BarbelHisto} in the {@link #populate(Collection)} method.<br>
     * <br>
     * Notice that this way of persistence to a custom data store is not the
     * recommended way for {@link BarbelHisto} persistence. {@link BarbelHisto} is
     * based on cqengine. There are build in on-heap (default), off-heap and disk
     * persistence options. See {@link DiskPersistence} and
     * {@link OffHeapPersistence} for details. <br>
     * 
     * @see <a href=
     *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
     * @return the collection of bitemporals to store into an arbitrary data store
     */
    Collection<Bitemporal> dump(DumpMode mode);

}
