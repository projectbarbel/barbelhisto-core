package org.projectbarbel.histo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.projectbarbel.histo.BarbelHistoCore.DumpMode;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.common.NonUniqueObjectException;

/**
 * The main abstraction of {@link BarbelHisto} that provides the client API.
 * Clients should perform all operations with this interface. <br>
 * <br>
 * Create an instance as follows: <br>
 * <br>
 * <code> BarbelHisto histo = BarbelHistoBuilder.barbel().build(); </code> <br>
 * <br>
 * Explore {@link BarbelHistoBuilder} documentation to learn about the different
 * settings you can choose.<br>
 * <br>
 * {@link BarbelHisto} tracks two time dimensions: <br>
 * - <b>effective time</b> is when a change to a domain object is supposed to
 * become effective <br>
 * - <b>record time</b> is when that change was recorded in the system <br>
 * <br>
 * Two {@link BarbelMode}s can be used to manage different types ob objects:
 * {@link BarbelMode#POJO} is the default mode and allows you to manage Pojos
 * anotated with {@link DocumentId} on the objects primary key. An example:
 * 
 * <pre>
 * public class SomeBusinessPojo {
 *    <code>@DocumentId</code>
 *    private String id;
 *    ... any custom fields and methods
 * }
 * </pre>
 * 
 * The primary key should be business oriented, i.e. personel number, contract
 * numer. Of course, using this pojo mode is the easiest way forward. However,
 * behind the scenes {@link BarbelHisto} uses proxying when managing pojos to
 * store the version data with the objects you save. Proxying can become
 * complicated in situations where no default contsructors are available, or
 * access to fields does not follow JavaBeans standard. For that reason there is
 * another mode called {@link BarbelMode#BITEMPORAL}. One can change the mode to
 * {@link BarbelMode#BITEMPORAL} with the
 * {@link BarbelHistoBuilder#withMode(BarbelMode)} method like so:
 * 
 * <pre>
 * BarbelHisto barbel = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL).build();
 * </pre>
 * 
 * If {@link BarbelMode#BITEMPORAL} is used clients have to implement the
 * interface {@link Bitemporal} on the type they wish to manage with
 * {@link BarbelHisto}. In this mode, there won't be any proxying magic applied
 * to objects. When clients implement the interface {@link Bitemporal} they need
 * to declare a field of type {@link BitemporalStamp} in the business object
 * type. There is nothing else required than declaring the field. Anything else
 * is managed by {@link BarbelHisto}. Here is an example:<br>
 * 
 * <pre>
 * public class SomeBusinessType implements Bitemporal {
 *    private BitemporalStamp stamp;
 *    ... any custom fields
 *    public BitemporalStamp getBitemporalStamp() {
 *      return stamp;
 *    }
 *    public void setBitemporalStamp(BitemporalStamp stamp) {
 *      this.stamp=stamp;
 *    }
 * }
 * </pre>
 * 
 * In any mode described you clients never have to care about the bitemporal
 * version info. This is completely managed by {@link BarbelHisto}. <br>
 * <br>
 * Use the {@link #save(Object, LocalDate, LocalDate)} method to save objects to
 * {@link BarbelHisto}. The from/until period you enter here is when the object
 * data should become effective and until it should be effective. Use
 * {@link LocalDate#MAX} to express that the object stored is valid until
 * infinite. <br>
 * <br>
 * To retrieve data you've stored into {@link BarbelHisto} you can use
 * {@link BarbelHisto#retrieveOne(Query)} or
 * {@link BarbelHisto#retrieve(Query)}. Use the {@link BarbelQueries} for your
 * convenience like so:
 * 
 * <pre>
 * List result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
 * 		BarbelQueryOptions.sortAscendingByEffectiveFrom());
 * </pre>
 * 
 * The id you enter in queries is the document id of your business object. In
 * {@link BarbelMode#POJO} it is the id annotated mit {@link DocumentId} like
 * described previously. in {@link BarbelMode#BITEMPORAL} you receive the id by
 * calling {@link Bitemporal#getBitemporalStamp()}.getDocumentId(). Be carefull
 * when to use {@link BarbelHisto#retrieveOne(Query)}. Not every query returns a
 * unique result. If you receive {@link NonUniqueObjectException} try
 * {@link BarbelHisto#retrieve(Query)} instead. <br>
 * <br>
 * Users should become familiar with all the queries in {@link BarbelQueries}.
 * <br>
 * <br>
 * Use {@link BarbelHisto#prettyPrintJournal(Object)} to understand the storage
 * of journals and what it means to manage bitemporal data in technical terms.
 * <br>
 * <br>
 * Clients that use custim data stores like MongoDB or any other of the type
 * should call {@link BarbelHisto#dump(DumpMode)} after processing data with
 * {@link BarbelHisto}. To restore the journal later to continue processing
 * bitemporal data use {@link BarbelHisto#populate(Collection)}.
 * 
 * @author Niklas Schlimm
 * 
 */
public interface BarbelHisto<T> {

	/**
	 * Save objects to {@link BarbelHisto}. Creates snapshots of state. Clients can
	 * safely continue to work on passed instances.
	 * 
	 * @param currentVersion the object state to save
	 * @param from           effective date of object state
	 * @param until          effective until of the state
	 * @return true if successful
	 */
	boolean save(T currentVersion, LocalDate from, LocalDate until);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} for there convenience here.
	 * {@link BarbelQueries} can be combined with additional queries from
	 * {@link QueryFactory}.
	 * 
	 * @param query the client query from {@link BarbelQueries} and/or {@link QueryFactory}
	 * @return the returned records, maybe empty, never null
	 */
	List<T> retrieve(Query<T> query);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} and {@link BarbelQueryOptions} for there
	 * convenience here. {@link BarbelQueries} can be combined with additional
	 * queries from {@link QueryFactory}.
	 * 
	 * @param query the client query from {@link BarbelQueries} and/or {@link QueryFactory}
	 * @param options the options from {@link BarbelQueryOptions} or {@link QueryFactory}
	 * @return the result list
	 */
	List<T> retrieve(Query<T> query, QueryOptions options);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} for there convenience here.
	 * {@link BarbelQueries} can be combined with additional queries from
	 * {@link QueryFactory}. Throws {@link NonUniqueObjectException} when query
	 * returns more then one result.
	 * 
	 * @param query the client query from {@link BarbelQueries} and/or {@link QueryFactory}
	 * @return the returned object as {@link Optional}
	 */
	Optional<T> retrieveOne(Query<T> query);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} and {@link BarbelQueryOptions} for there
	 * convenience here. {@link BarbelQueries} can be combined with additional
	 * queries from {@link QueryFactory}. Throws {@link NonUniqueObjectException} when query
	 * returns more then one result.
	 * 
	 * @param query the client query from {@link BarbelQueries} and/or {@link QueryFactory}
	 * @param options the options from {@link BarbelQueryOptions} or {@link QueryFactory}
	 * @return the returned object as {@link Optional}
	 */
	Optional<T> retrieveOne(Query<T> query, QueryOptions options);

	DocumentJournal timeshift(Object id, LocalDateTime time);

	String prettyPrintJournal(Object id);

	/**
	 * Method for clients that use a custom data store. It's recomended to only add
	 * complete journals previously created by {@link BarbelHisto}. That means only
	 * add complete set of versions for one or more document IDs. To achieve this,
	 * preferably use this method with version collections produced by
	 * {@link #dump(DumpMode)} method. That ensures consistent state with
	 * intermediate persistence operations to custom data stores. You can also use
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
	 * to relaod {@link BarbelHisto} in the {@link #populate(Collection)}
	 * method.<br>
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
