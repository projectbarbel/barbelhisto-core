package org.projectbarbel.histo;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.projectbarbel.histo.BarbelHistoCore.UpdateLogRecord;
import org.projectbarbel.histo.functions.AdaptingKryoSerializer;
import org.projectbarbel.histo.functions.CachingCGLibProxyingFunction;
import org.projectbarbel.histo.functions.RitsClonerCopyFunction;
import org.projectbarbel.histo.functions.TableJournalPrettyPrinter;
import org.projectbarbel.histo.functions.UUIDGenerator;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.Systemclock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

/**
 * Delivers the configuration set by {@link BarbelHistoBuilder} to the
 * application components.
 * 
 * @author Niklas Schlimm
 *
 */
public interface BarbelHistoContext {

	static <T> Supplier<IndexedCollection<T>> getDefaultBackbone() {
		return ConcurrentIndexedCollection::new;
	}

	static Function<BarbelHistoContext, PojoSerializer<Bitemporal>> getDefaultPersistenceSerializerProducer() {
		return AdaptingKryoSerializer::new;
	}

	static Function<List<Bitemporal>, String> getDefaultPrettyPrinter() {
		return new TableJournalPrettyPrinter();
	}

	static IndexedCollection<UpdateLogRecord> getDefaultUpdateLog() {
		return new ConcurrentIndexedCollection<>();
	}

	static BarbelMode getDefaultBarbelMode() {
		return BarbelMode.POJO;
	}

	static String getDefaultActivity() {
		return BarbelHistoBuilder.SYSTEMACTIVITY;
	}

	static LocalDate getInfiniteDate() {
		return LocalDate.MAX;
	}

	static Systemclock getBarbelClock() {
		return BarbelHistoBuilder.CLOCK;
	}

	static Supplier<Object> getDefaultDocumentIDGenerator() {
		return new UUIDGenerator();
	}

	static Supplier<Object> getDefaultVersionIDGenerator() {
		return new UUIDGenerator();
	}

	static String getDefaultUser() {
		return BarbelHistoBuilder.SYSTEM;
	}

	static Supplier<BiFunction<Object, BitemporalStamp, Object>> getDefaultProxyingFunctionSupplier() {
		return () -> CachingCGLibProxyingFunction.INSTANCE;
	}

	static Gson getDefaultGson() {
		return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, BarbelHistoBuilder.ZDT_DESERIALIZER)
				.registerTypeAdapter(ZonedDateTime.class, BarbelHistoBuilder.ZDT_SERIALIZER).create();
	}

	static Supplier<UnaryOperator<Object>> getDefaultCopyFunctionSupplier() {
		return () -> RitsClonerCopyFunction.INSTANCE;
	}

	Supplier<Object> getVersionIdGenerator();

	<T> Supplier<IndexedCollection<T>> getBackboneSupplier();

	String getActivity();

	String getUser();

	Map<Object, DocumentJournal> getJournalStore();

	Supplier<BiFunction<Object, BitemporalStamp, Object>> getPojoProxyingFunctionSupplier();

	Supplier<UnaryOperator<Object>> getPojoCopyFunctionSupplier();

	Gson getGson();

	Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> getJournalUpdateStrategyProducer();

	BarbelMode getMode();

	IndexedCollection<UpdateLogRecord> getUpdateLog();

	Function<List<Bitemporal>, String> getPrettyPrinter();

	Function<BarbelHistoContext, PojoSerializer<Bitemporal>> getPersistenceSerializerProducer();

	Map<String, Object> getContextOptions();

}
