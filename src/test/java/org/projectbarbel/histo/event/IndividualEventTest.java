package org.projectbarbel.histo.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.event.Events.AcquireLockEvent;
import org.projectbarbel.histo.event.Events.InitializeJournalEvent;
import org.projectbarbel.histo.event.Events.InsertBitemporalEvent;
import org.projectbarbel.histo.event.Events.ReleaseLockEvent;
import org.projectbarbel.histo.event.Events.ReplaceBitemporalEvent;
import org.projectbarbel.histo.event.Events.RetrieveDataEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.ConcurrentIndexedCollection;

public class IndividualEventTest {

	private static List<Object> list = new ArrayList<Object>();

    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(
                Arguments.of(Events.ACQUIRELOCK),
                Arguments.of(Events.INITIALIZEJOURNAL),
                Arguments.of(Events.INSERTBITEMPORAL),
                Arguments.of(Events.RELEASELOCK),
                Arguments.of(Events.RETRIEVEDATA),
                Arguments.of(Events.REPLACEBITEMPORAL)
                );
    }
    
    @ParameterizedTest
    @MethodSource("createPojos")
	void testSynchronous(Events event) throws Exception {
		list.clear();
		BarbelHistoContext context = BarbelHistoBuilder.barbel();
		EventBus bus = context.getSynchronousEventBus();
		bus.register(new EventTestListener());
		event.create().with(DocumentJournal.create(ProcessingState.INTERNAL,
                BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")).postSynchronous(context);
        waitForEventToComplete(1);
		assertEquals(1, list.size());
	}

    @ParameterizedTest
    @MethodSource("createPojos")
    void testAsynchronous(Events event) throws Exception {
        list.clear();
        BarbelHistoContext context = BarbelHistoBuilder.barbel();
        EventBus bus = context.getAsynchronousEventBus();
        bus.register(new EventTestListener());
        event.create().with(DocumentJournal.create(ProcessingState.INTERNAL,
                BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")).postAsynchronous(context);
        waitForEventToComplete(1);
        assertEquals(1, list.size());
    }
    
    @ParameterizedTest
    @MethodSource("createPojos")
    void testAbroad(Events event) throws Exception {
        list.clear();
        BarbelHistoContext context = BarbelHistoBuilder.barbel();
        EventBus bus1 = context.getAsynchronousEventBus();
        EventBus bus2 = context.getAsynchronousEventBus();
        bus1.register(new EventTestListener());
        bus2.register(new EventTestListener());
        event.create().with(DocumentJournal.create(ProcessingState.INTERNAL,
                BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")).postAbroad(context);
        waitForEventToComplete(2);
        assertEquals(2, list.size());
    }
    
	public static class ExceptionThrowingListener {
		@Subscribe
		public void handle(AcquireLockEvent initialize) throws InterruptedException {
			throw new ConcurrentModificationException("already locked");
		}
	}

	public static class EventTestListener {
		@Subscribe
		public void handle(RetrieveDataEvent event) throws InterruptedException {
			assertNotNull(event);
			notifyCallerThread();
		}

		@Subscribe
		public void handle(InitializeJournalEvent event) throws InterruptedException {
            assertNotNull(event);
			notifyCallerThread();
		}
		
		@Subscribe
		public void handle(InsertBitemporalEvent event) throws InterruptedException {
            assertNotNull(event);
			notifyCallerThread();
		}

		@Subscribe
		public void handle(ReleaseLockEvent event) throws InterruptedException {
            assertNotNull(event);
			notifyCallerThread();
		}

		@Subscribe
		public void handle(AcquireLockEvent event) throws InterruptedException {
            assertNotNull(event);
			notifyCallerThread();
		}
		
		@Subscribe
		public void handle(ReplaceBitemporalEvent event) throws InterruptedException {
            assertNotNull(event);
			notifyCallerThread();
		}
	}

	private static void waitForEventToComplete(int syncpoint) throws InterruptedException {
		synchronized (list) {
			while (list.size()<syncpoint) {
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
