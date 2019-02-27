package org.projectbarbel.histo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;

import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * The main abstraction of {@link BarbelHisto} that provides the client API.
 * Clients should perform all operations with this interface. <br>
 * <br>
 * Create an instance as follows: <br>
 * <br>
 * <code> BarbelHisto histo = BarbelHistoBuilder.barbel().build(); </code> <br>
 * <br>
 * Explore {@link BarbelHistoBuilder} documentation to learn about the different
 * settings clients can choose.<br>
 * <br>
 * {@link BarbelHisto} tracks two time dimensions: <br>
 * - <b>effective time</b> is when a change to a domain object is supposed to
 * become effective <br>
 * - <b>record time</b> is when that change was recorded in the system, when
 * records were created and inactivated <br>
 * <br>
 * To manage data with {@link BarbelHisto} clients must annotate their classes
 * with {@link DocumentId} on the primary key. An example:
 * 
 * <pre>
 * public class SomeBusinessPojo {
 *    <code>@DocumentId</code>
 *    private String documentId;
 *    ... any custom fields and methods
 *    public String getDocumentId() {
 *       return documentId;
 *    }
 *    public void setDocumentId(String id) {
 *       this.documentId = id;
 *    }
 * }
 * </pre>
 * 
 * The primary key should be business oriented, i.e. personnel number, contract
 * number. <br>
 * <br>
 * Two {@link BarbelMode}s can be used to manage different types of objects:
 * {@link BarbelMode#POJO} is the default mode. Of course, using POJO mode is
 * the easiest way forward. However, behind the scenes {@link BarbelHisto} uses
 * proxying when managing POJOs to store the version data with the objects that
 * clients save. Proxying can become complicated in some situations. For that
 * reason there is another mode called {@link BarbelMode#BITEMPORAL}. One can
 * change the mode to {@link BarbelMode#BITEMPORAL} with the
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
 * is managed by {@link BarbelHisto}. See {@link DefaultDocument} as an example
 * of a fully equipped business class in {@link BarbelMode#BITEMPORAL}. <br>
 * <br>
 * In any mode clients <b>never</b> have to care about the bitemporal version
 * stamp. This is completely managed by {@link BarbelHisto}. Clients only need
 * to take care that their primary key is properly set.<br>
 * <br>
 * Use the {@link #save(Object, LocalDate, LocalDate)} method to save objects to
 * {@link BarbelHisto}. The from/until period entered describes when the object
 * data should become effective, and until it should be effective. Clients use
 * {@link LocalDate#MAX} to express that the object stored is valid until
 * infinite. <br>
 * <br>
 * To retrieve data stored into {@link BarbelHisto} clients can use
 * {@link BarbelHisto#retrieveOne(Query)} or
 * {@link BarbelHisto#retrieve(Query)}. Use the {@link BarbelQueries} for
 * convenience like so:
 * 
 * <pre>
 * List result = core.retrieve(BarbelQueries.allActive(someBusinessPojo.getDocumentId()),
 * 		BarbelQueryOptions.sortAscendingByEffectiveFrom());
 * </pre>
 * 
 * Users should become familiar with all the queries in {@link BarbelQueries}.
 * <br>
 * <br>
 * Clients that use custom data stores like MongoDB or any other of the kind
 * should call {@link BarbelHisto#unload(Object...)} after processing data with
 * {@link BarbelHisto}. To restore the journal later to continue bitemporal data
 * processing use {@link BarbelHisto#load(Collection)}. <br>
 * <br>
 * Use {@link #timeshift(Object, LocalDateTime)} to turn back time and see how
 * document journals looked like in the past. Time shift is one of the core
 * functionalities of {@link BarbelHisto}. Clients can turn back time to see how
 * the document journals for a document Id looked like at that given time. This
 * knowledge is mission critical for many businesses. Notice that this method
 * returns complete journals of a given document Id. It is often the case that
 * objects effective periods change over time as updates are posted to
 * {@link BarbelHisto} with different effective periods. Today a document may
 * have two active effective periods, one period A starting two years ago, and
 * another one period B effective beginning in two weeks, e.g. an adress change
 * to an initial client record that the client communicated two weeks ago.
 * 
 * <pre>
 * Today: 2019-02-01
 * Effective from   Effective Until     Created at      Adress
 * 2017-01-01       2019-02-14          2016-12-15      Barbel-Street 1    Period A
 * 2019-02-14       Infinite            2019-01-15      Carp-Street 10     Period B
 * </pre>
 * 
 * In this example three weeks ago on January 9, 2019 that adress change in
 * question was not recorded to the system. If you turn back time to January 9,
 * 2019 that journal returned by time shift would only contain the effective
 * period A. These cases can get more complex obviously the more updates are
 * posted for a given document Id. Use
 * {@link BarbelHisto#prettyPrintJournal(Object)} to get more familiar with
 * effective periods and record time. <br>
 * <br>
 * <br>
 * 
 * @author Niklas Schlimm
 * 
 * @param <T> the type to manage
 */
public interface BarbelHisto<T> {

	/**
	 * Save objects to {@link BarbelHisto}. Creates snapshots of state. Clients can
	 * safely continue to work on passed instances. Thread safe, applies lock to
	 * journals of document IDs (not the complete backbone). This allows concurrent
	 * work on different document IDs. If clients try to update the same document
	 * Id, this method throws {@link ConcurrentModificationException}.
	 * 
	 * The method returns a copy of the object saved including the version data. In
	 * in {@link BarbelMode#POJO} cast the returned object to {@link Bitemporal} to
	 * read the version data.
	 * 
	 * @param newVersion the object state to save
	 * @param from       effective date of object state
	 * @param until      effective until of the state
	 * @return the saved object including the version data
	 */
	T save(T newVersion, LocalDate from, LocalDate until);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} for there convenience here.
	 * {@link BarbelQueries} can be combined with additional queries from
	 * {@link QueryFactory}.
	 * 
	 * @param query the client query from {@link BarbelQueries} and/or
	 *              {@link QueryFactory}
	 * @return the copies returned, maybe empty, never null
	 */
	List<T> retrieve(Query<T> query);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} and {@link BarbelQueryOptions} for there
	 * convenience here. {@link BarbelQueries} can be combined with additional
	 * queries from {@link QueryFactory}.
	 * 
	 * @param query   the client query from {@link BarbelQueries} and/or
	 *                {@link QueryFactory}
	 * @param options the options from {@link BarbelQueryOptions} or
	 *                {@link QueryFactory}
	 * @return the copies returned, maybe empty, never null
	 */
	List<T> retrieve(Query<T> query, QueryOptions options);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} for there convenience here.
	 * {@link BarbelQueries} can be combined with additional queries from
	 * {@link QueryFactory}. Throws {@link IllegalStateException} when
	 * query returns more then one result, {@link NoSuchElementException} if nothing
	 * was found.
	 * 
	 * @param query the client query from {@link BarbelQueries} and/or
	 *              {@link QueryFactory}
	 * @return the returned copy
	 */
	T retrieveOne(Query<T> query);

	/**
	 * Retrieve data from {@link BarbelHisto} using cqengine like queries. Clients
	 * want to use {@link BarbelQueries} and {@link BarbelQueryOptions} for there
	 * convenience here. {@link BarbelQueries} can be combined with additional
	 * queries from {@link QueryFactory}. Throws {@link IllegalStateException} when
	 * query returns more then one result, {@link NoSuchElementException} if nothing
	 * was found.
	 * 
	 * @param query   the client query from {@link BarbelQueries} and/or
	 *                {@link QueryFactory}
	 * @param options the options from {@link BarbelQueryOptions} or
	 *                {@link QueryFactory}
	 * @return the returned copy
	 */
	T retrieveOne(Query<T> query, QueryOptions options);

	/**
	 * Turn back time to see how document journals looked like in the past. If
	 * clients pass {@link LocalDateTime#now()} the actual journal is returned. Time
	 * shift does <b>not</b> change the backbone collection of the
	 * {@link BarbelHisto} instance, instead it returns an instance of
	 * {@link DocumentJournal} with copies of managed {@link Bitemporal} objects.
	 * 
	 * @param id   the document Id
	 * @param time the time, must be in the past
	 * @return the document journal at that given time
	 */
	DocumentJournal timeshift(Object id, LocalDateTime time);

	/**
	 * Pretty print the journal for the given document ID.
	 * 
	 * @param id the document ID
	 * @return the journal as pretty print out
	 */
	String prettyPrintJournal(Object id);

	/**
	 * Method for clients that use a custom data store. Only add complete set of
	 * versions for one or more document IDs. Usually used in conjunction with
	 * {@link #unload(Object...)}.<br>
	 * <br>
	 * In {@link BarbelMode#POJO} (default) clients have to pass a collection of
	 * {@link BitemporalVersion}. In {@link BarbelMode#BITEMPORAL} mode clients can
	 * add objects that implement {@link Bitemporal}. <br>
	 * <br>
	 * 
	 * @see <a href=
	 *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
	 * @param bitemporals consistent list of {@link Bitemporal} versions
	 */
	void load(Collection<Bitemporal> bitemporals);

	/**
	 * Unloads the journal data of the given document IDs into a collection and
	 * return that to the client. The journal data of the given document IDs will be
	 * deleted from the backbone. This method is used when client uses custom data
	 * store. Clients may store the returned collection to the data store of their
	 * choice. Used in conjunction with {@link #load(Collection)} to re-load that
	 * stored journals back into {@link BarbelHisto} to continue bitemporal
	 * processing.<br>
	 * <br>
	 * In {@link BarbelMode#POJO} (default) clients receive a collection of
	 * {@link BitemporalVersion} objects. In {@link BarbelMode#BITEMPORAL} clients
	 * receive objects that implement {@link Bitemporal}. <br>
	 * <br>
	 * 
	 * @see <a href=
	 *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
	 * @param documentIDs the document IDs to unload
	 * @return the collection of {@link Bitemporal} objects to store into an
	 *         arbitrary data store
	 *
	 */
	Collection<Bitemporal> unload(Object... documentIDs);
	
	/**
	 * Check if {@link BarbelHisto} contains data for the given document ID.
	 * 
	 * @param documentId the document id to check
	 * @return true if {@link BarbelHisto} contains data otherwise false
	 */
	boolean contains(Object documentId);

}
