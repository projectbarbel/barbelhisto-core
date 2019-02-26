package org.projectbarbel.histo.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.event.EventType.AcquireLockEvent;
import org.projectbarbel.histo.event.EventType.InitializeJournalEvent;
import org.projectbarbel.histo.event.EventType.InsertBitemporalEvent;
import org.projectbarbel.histo.event.EventType.ReleaseLockEvent;
import org.projectbarbel.histo.event.EventType.ReplaceBitemporalEvent;
import org.projectbarbel.histo.event.EventType.RetrieveDataEvent;
import org.projectbarbel.histo.event.EventType.UpdateFinishedEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.googlecode.cqengine.ConcurrentIndexedCollection;

public class IndividualEventTest {

	private static List<Object> list = new ArrayList<Object>();

    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(
                Arguments.of(EventType.ACQUIRELOCK),
                Arguments.of(EventType.INITIALIZEJOURNAL),
                Arguments.of(EventType.INSERTBITEMPORAL),
                Arguments.of(EventType.RELEASELOCK),
                Arguments.of(EventType.RETRIEVEDATA),
                Arguments.of(EventType.UPDATEFINISHED),
                Arguments.of(EventType.REPLACEBITEMPORAL)
                );
    }
    
    @ParameterizedTest
    @MethodSource("createPojos")
	void testSynchronous(EventType event) throws Exception {
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
    void testSynchronous_Failing(EventType event) throws Exception {
        list.clear();
        BarbelHistoContext context = BarbelHistoBuilder.barbel();
        EventBus bus = context.getSynchronousEventBus();
        bus.register(new ExceptionThrowingListener());
        HistoEventFailedException exception = assertThrows(HistoEventFailedException.class, ()->event.create().with(DocumentJournal.create(ProcessingState.INTERNAL,
                BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")).postSynchronous(context));
        assertEquals(event, exception.getEvent().getEventType());
    }
    
    @ParameterizedTest
    @MethodSource("createPojos")
    void testAsynchronous(EventType event) throws Exception {
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
    void testAbroad(EventType event) throws Exception {
        list.clear();
        BarbelHistoContext context = BarbelHistoBuilder.barbel();
        EventBus bus1 = context.getAsynchronousEventBus();
        EventBus bus2 = context.getAsynchronousEventBus();
        bus1.register(new EventTestListener());
        bus2.register(new EventTestListener());
        event.create().with(DocumentJournal.create(ProcessingState.INTERNAL,
                BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<>(), "someId")).postBothWay(context);
        waitForEventToComplete(2);
        assertEquals(2, list.size());
    }
    
	public static class ExceptionThrowingListener {
        @Subscribe
        public void handle(RetrieveDataEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
        }

        @Subscribe
        public void handle(InitializeJournalEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
        }
        
        @Subscribe
        public void handle(InsertBitemporalEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
        }

        @Subscribe
        public void handle(ReleaseLockEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
        }

        @Subscribe
        public void handle(AcquireLockEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
        }
        
        @Subscribe
        public void handle(ReplaceBitemporalEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
        }
        @Subscribe
        public void handle(UpdateFinishedEvent event) throws InterruptedException {
            event.failed(new NullPointerException());
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
		@Subscribe
        public void handle(UpdateFinishedEvent event) throws InterruptedException {
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
