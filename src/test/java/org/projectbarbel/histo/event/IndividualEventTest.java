package org.projectbarbel.histo.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.query.QueryFactory;

public class IndividualEventTest {

	private static List<Object> list = new ArrayList<Object>();

	@Test
	void testSynchronousInitialize() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new EventTestListener());
		bus.post(new InitializeJournalEvent(DocumentJournal.create(ProcessingState.INTERNAL,
				BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")));
		assertEquals(1, list.size());
	}

	@Test
	void testAsynchronousInitialize() throws Exception {
		list.clear();
		AsyncEventBus bus = BarbelHistoBuilder.barbel().getAsynchronousEventBus();
		EventTestListener listener = new EventTestListener();
		bus.register(listener);
		bus.post(new InitializeJournalEvent(DocumentJournal.create(ProcessingState.INTERNAL,
				BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")));
		waitForEventToComplete();
		assertEquals(1, list.size());
	}

	@Test
	void testSynchronousAcquire() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new EventTestListener());
		bus.post(new AcquireLockEvent(DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				new ConcurrentIndexedCollection<>(), "someId")));
		assertEquals(1, list.size());
	}

	@Test
	void testSynchronousAcquire_LockedAlready() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new ExceptionThrowingListener());
		bus.post(new AcquireLockEvent(DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				new ConcurrentIndexedCollection<>(), "someId")));
		assertEquals(0, list.size());
	}

	@Test
	void testSynchronousInsert() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new EventTestListener());
		bus.post(new InsertBitemporalEvent(DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				new ConcurrentIndexedCollection<>(), "someId"), Arrays.asList(new DefaultDocument())));
		assertEquals(1, list.size());
	}

	@Test
	void testSynchronousRelease() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new EventTestListener());
		bus.post(new ReleaseLockEvent(DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				new ConcurrentIndexedCollection<>(), "someId")));
		assertEquals(1, list.size());
	}

	@Test
	void testSynchronousReplace() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new EventTestListener());
		bus.post(new ReplaceBitemporalEvent(
				DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
						new ConcurrentIndexedCollection<>(), "someId"),
				Arrays.asList(new DefaultDocument()), Arrays.asList(new DefaultDocument())));
		assertEquals(1, list.size());
	}

	@Test
	void testSynchronousRetrieve() throws Exception {
		list.clear();
		EventBus bus = BarbelHistoBuilder.barbel().getSynchronousEventBus();
		bus.register(new EventTestListener());
		bus.post(new RetrieveDataEvent(BarbelHistoBuilder.barbel(), QueryFactory.equal(DefaultPojo.DOCUMENT_ID, "string")));
		assertEquals(1, list.size());
	}
	
	public static class ExceptionThrowingListener {
		@Subscribe
		public void handle(AcquireLockEvent initialize) throws InterruptedException {
			throw new ConcurrentModificationException("already locked");
		}
	}

	public static class EventTestListener {
		@Subscribe
		public void handle(RetrieveDataEvent initialize) throws InterruptedException {
			assertNotNull(initialize.getContext());
			notifyCallerThread();
		}

		@Subscribe
		public void handle(InitializeJournalEvent initialize) throws InterruptedException {
			assertNotNull(initialize.getDocumentJournal());
			notifyCallerThread();
		}
		
		@Subscribe
		public void handle(InsertBitemporalEvent initialize) throws InterruptedException {
			assertNotNull(initialize.getDocumentJournal());
			notifyCallerThread();
		}

		@Subscribe
		public void handle(ReleaseLockEvent initialize) throws InterruptedException {
			assertNotNull(initialize.getDocumentJournal());
			notifyCallerThread();
		}

		@Subscribe
		public void handle(AcquireLockEvent initialize) throws InterruptedException {
			assertNotNull(initialize.getDocumentJournal());
			notifyCallerThread();
		}
		
		@Subscribe
		public void handle(ReplaceBitemporalEvent initialize) throws InterruptedException {
			assertNotNull(initialize.getDocumentJournal());
			notifyCallerThread();
		}
	}

	private static void waitForEventToComplete() throws InterruptedException {
		synchronized (list) {
			while (list.isEmpty()) {
				list.wait();
			}
		}
	}

	private static void notifyCallerThread() {
		synchronized (list) {
			list.add(new Object());
			list.notifyAll();
		}
	}

}
