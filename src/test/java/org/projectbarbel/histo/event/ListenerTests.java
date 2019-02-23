package org.projectbarbel.histo.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.event.AcquireLockEvent.AbstractAcquireLockListener;

import com.googlecode.cqengine.ConcurrentIndexedCollection;

public class ListenerTests {

	@Test
	void abstractAcquireLockListener_failing() throws Exception {
		BarbelHistoContext context = BarbelHistoBuilder.barbel().withSynchronousEventListener(new FailingEventListener());
		AcquireLockEvent event = new AcquireLockEvent(DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				new ConcurrentIndexedCollection<>(), "someId"));
		context.postEvent(event);
		assertFalse(event.succeeded());
	}
	
	@Test
	void abstractAcquireLockListener() throws Exception {
		BarbelHistoContext context = BarbelHistoBuilder.barbel().withSynchronousEventListener(new SucceedingEventListener());
		AcquireLockEvent event = new AcquireLockEvent(DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				new ConcurrentIndexedCollection<>(), "someId"));
		context.postEvent(event);
		assertTrue(event.succeeded());
	}
	
	private static class FailingEventListener extends AbstractAcquireLockListener {

		@Override
		protected void doHanlde(AcquireLockEvent event) {
			throw new NullPointerException();
		}
		
	}
	
	private static class SucceedingEventListener extends AbstractAcquireLockListener {
		
		@Override
		protected void doHanlde(AcquireLockEvent event) {
			// NOP
		}
		
	}
}
