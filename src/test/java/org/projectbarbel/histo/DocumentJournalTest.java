package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

public class DocumentJournalTest {

	@Test
	public void testCreate_withList() {
		DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				BarbelTestHelper.generateJournalOfDefaultDocuments("#12345",
						Arrays.asList(ZonedDateTime.parse("2019-01-01T00:00:00Z"), ZonedDateTime.parse("2019-04-01T00:00:00Z"))),
				"#12345");
		assertEquals(2, journal.size());
	}

	@Test
	public void testCreate_withList_differentDocumentIds() throws Exception {
		DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(),
				BarbelTestHelper.asIndexedCollection(BarbelTestHelper.random(DefaultDocument.class),
						BarbelTestHelper.random(DefaultDocument.class)),
				"arbitrary");
		assertTrue(journal.size() == 0);
	}

	@Test
	public void testCreate_withList_Empty() throws Exception {
		IndexedCollection<Object> list = new ConcurrentIndexedCollection<Object>();
		DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(), list,
				"");
		assertNotNull(journal);
	}

	@Test
	public void testCreate_withList_null() throws Exception {
		IndexedCollection<Object> list = null;
		assertThrows(NullPointerException.class,
				() -> DocumentJournal.create(ProcessingState.INTERNAL, BarbelHistoBuilder.barbel(), list, ""));
	}

	@Test
	public void testUpdate() throws Exception {
		IndexedCollection<Object> coll = new ConcurrentIndexedCollection<Object>();
		BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 2, 1, 8, 0).atZone(ZoneId.of("Z")));
		DefaultDocument doc = DefaultDocument.builder().withData("some data")
				.withBitemporalStamp(BitemporalStamp.createActive()).build();
		coll.add(doc);
		DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL,
				BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL), coll,
				doc.getBitemporalStamp().getDocumentId());
		journal.insert(Arrays.asList(doc));
		assertTrue(journal.list().size() == 1);
		BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
	}

	@Test
	public void testList() throws Exception {
		DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL,
				BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL),
				BarbelTestHelper.generateJournalOfDefaultDocuments("#12345",
						Arrays.asList(ZonedDateTime.parse("2019-01-01T00:00:00Z"), ZonedDateTime.parse("2019-04-01T00:00:00Z"))),
				"#12345");
		assertEquals(((Bitemporal) journal.list().get(0)).getBitemporalStamp().getEffectiveTime().from(),
				ZonedDateTime.parse("2019-01-01T00:00:00Z"));
	}

}
