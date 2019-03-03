package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;

import com.googlecode.cqengine.IndexedCollection;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelModeTest {

	@Test
	public void testGetIdValue() throws Exception {
		DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
		assertEquals(pojo.getDocumentId(), BarbelMode.POJO.getIdValue(pojo).get());
	}

	@Test
	public void testSnapshotManagedBitemporal() throws Exception {
		Bitemporal managed = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.createActive());
		Bitemporal bitemporal = BarbelMode.POJO.snapshotManagedBitemporal(BarbelHistoBuilder.barbel(), managed,
				BitemporalStamp.createActive());
		assertNotEquals(managed, bitemporal);
		assertEquals(((BarbelProxy) managed).getTarget(), ((BarbelProxy) bitemporal).getTarget());
	}

	@Test
	public void testSnapshotManagedBitemporal_Bitemporal() throws Exception {
		Bitemporal managed = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(),
				new BitemporalVersion(BitemporalStamp.createActive(),
						EnhancedRandom.random(DefaultPojo.class)),
				BitemporalStamp.createActive());
		Bitemporal bitemporal = BarbelMode.POJO.snapshotManagedBitemporal(BarbelHistoBuilder.barbel(), managed,
				BitemporalStamp.createActive());
		assertNotEquals(managed, bitemporal); // stamps differ
		assertEquals(((BarbelProxy) managed).getTarget(), ((BarbelProxy) bitemporal).getTarget());
	}

	@Test
	public void testDrawDocumentId() throws Exception {
		assertNotNull(BarbelMode.POJO.drawDocumentId(EnhancedRandom.random(DefaultPojo.class)));
	}

	@Test
	public void testSnapshotMaiden() throws Exception {
		Bitemporal managed = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.createActive());
		assertTrue(managed instanceof BarbelProxy);
		assertTrue(managed instanceof Bitemporal);
	}

	@Test
	public void testManagedBitemporalToCustomPersistenceObjects() throws Exception {
		assertTrue(
				BarbelMode.POJO
						.managedBitemporalToCustomPersistenceObjects("some",
								BarbelTestHelper.generateJournalOfManagedDefaultPojos(BarbelHistoBuilder.barbel(),"some",
										Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 3, 1))))
						.size() == 2);
	}

	@Test
	public void testManagedBitemporalToCustomPersistenceObjectsTwoJournals() throws Exception {
		IndexedCollection<Object> bitemporals = BarbelTestHelper.generateJournalOfManagedDefaultPojos(BarbelHistoBuilder.barbel(),"some",
				Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 3, 1)));
		bitemporals.addAll(BarbelTestHelper.generateJournalOfManagedDefaultPojos(BarbelHistoBuilder.barbel(),"other",
				Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 3, 1))));
		assertTrue(BarbelMode.POJO.managedBitemporalToCustomPersistenceObjects("some", bitemporals).size() == 2);
	}

	@Test
	public void testCustomPersistenceObjectsToManagedBitemporals() throws Exception {
		assertTrue(BarbelMode.POJO.customPersistenceObjectsToManagedBitemporals(BarbelHistoBuilder.barbel(),
				Arrays.asList(BarbelTestHelper.random(BitemporalVersion.class),
						BarbelTestHelper.random(BitemporalVersion.class)))
				.size() == 2);
	}

	@Test
	public void testCustomPersistenceObjectsToManagedBitemporals_Wrongtype() throws Exception {
		assertThrows(IllegalArgumentException.class,
				() -> BarbelMode.POJO.customPersistenceObjectsToManagedBitemporals(BarbelHistoBuilder.barbel(),
						Arrays.asList(BarbelTestHelper.random(DefaultDocument.class),
								BarbelTestHelper.random(DefaultDocument.class))));
	}

	@Test
	public void testCopyManagedBitemporal() throws Exception {
		Bitemporal managed = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.createActive());
		Bitemporal copy = BarbelMode.POJO.copyManagedBitemporal(BarbelHistoBuilder.barbel(), managed);
		assertEquals(managed, copy);
		assertNotSame(managed, copy);
	}

	@Test
    	public void testValidateMaidenCandidate() throws Exception {
    		assertThrows(IllegalArgumentException.class, () -> BarbelMode.POJO
    				.validateMaidenCandidate(BarbelHistoBuilder.barbel(), BarbelTestHelper.random(BitemporalVersion.class)));
    	}

	@Test
    	public void testValidateMaidenCandidate_MissingDocId() throws Exception {
    		assertThrows(IllegalArgumentException.class, () -> BarbelMode.POJO
    				.validateMaidenCandidate(BarbelHistoBuilder.barbel(), BarbelTestHelper.random(wodocid.class)));
    	}

	@SuppressWarnings("unused")
	private static class wodocid {
		private String some;
	}

	@Test
	public void testDrawMaiden_POJO() throws Exception {
		DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
		Object maiden = BarbelMode.POJO.drawMaiden(BarbelHistoBuilder.barbel(), pojo);
		assertEquals(pojo, maiden);
	}

	@Test
	public void testDrawMaiden_POJO_Proxy() throws Exception {
		DefaultPojo pojo = (DefaultPojo) BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.createActive());
		Object maiden = BarbelMode.POJO.drawMaiden(BarbelHistoBuilder.barbel(), pojo);
		assertNotEquals(pojo, maiden);
		assertEquals(((BarbelProxy) pojo).getTarget(), maiden);
	}

	@Test
	public void testDrawMaiden_Bizemporal() throws Exception {
		DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
		Bitemporal managed = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), pojo,
				BitemporalStamp.createActive());
		Object maiden = BarbelMode.POJO.drawMaiden(BarbelHistoBuilder.barbel(), managed);
		assertEquals(pojo, maiden);
	}

	@Test
	public void testGetPersistenceObjectType() throws Exception {
		assertEquals(BarbelMode.POJO.getPersistenceObjectType(Object.class), BitemporalVersion.class);
	}

	public void testGetIdValue_Bitemporal() throws Exception {
		DefaultDocument pojo = BarbelTestHelper.random(DefaultDocument.class);
		assertEquals(pojo.getId(), BarbelMode.POJO.getIdValue(pojo).get());
	}

    @Test
    public void testManagedBitemporalToCustomPersistenceObject_PojoMode_Failure() throws Exception {
        DefaultDocument doc = new DefaultDocument(BitemporalStamp.createActive(), "some data");
        assertThrows(ClassCastException.class, ()->BarbelMode.POJO.managedBitemporalToCustomPersistenceObject(doc));
    }

    @Test
    public void testManagedBitemporalToCustomPersistenceObject_PojoMode() throws Exception {
        DefaultPojo pojo = new DefaultPojo("id","data");
        BitemporalStamp stamp = BitemporalStamp.createActive();
        Bitemporal doc = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), pojo, stamp);
        assertEquals(new BitemporalVersion(stamp, pojo), BarbelMode.POJO.managedBitemporalToCustomPersistenceObject(doc));
    }
    
	@Test
	public void testSnapshotManagedBitemporal_BitemporalMode() throws Exception {
		Bitemporal managed = BarbelMode.BITEMPORAL.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultDocument.class), BitemporalStamp.createActive());
		Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotManagedBitemporal(BarbelHistoBuilder.barbel(), managed,
				BitemporalStamp.createActive());
		assertNotEquals(managed, bitemporal);
		assertNotEquals(bitemporal.getBitemporalStamp(), managed.getBitemporalStamp());
	}

	@Test
	public void testSnapshotManagedBitemporal_Bitemporal_BitemporalMode() throws Exception {
		Bitemporal managed = BarbelMode.BITEMPORAL.snapshotMaiden(BarbelHistoBuilder.barbel(),
				new BitemporalVersion(BitemporalStamp.createActive(),
						EnhancedRandom.random(DefaultDocument.class)),
				BitemporalStamp.createActive());
		Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotManagedBitemporal(BarbelHistoBuilder.barbel(), managed,
				BitemporalStamp.createActive());
		assertNotEquals(managed, bitemporal); // stamps differ
	}

	@Test
	public void testDrawDocumentId_BitemporalMode() throws Exception {
		assertNotNull(BarbelMode.BITEMPORAL.drawDocumentId(EnhancedRandom.random(DefaultDocument.class)));
	}

	@Test
	public void testSnapshotMaiden_BitemporalMode() throws Exception {
		Bitemporal managed = BarbelMode.BITEMPORAL.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultDocument.class), BitemporalStamp.createActive());
		assertTrue(managed instanceof Bitemporal);
	}

	@Test
	public void testManagedBitemporalToCustomPersistenceObjects_BitemporalMode() throws Exception {
		assertTrue(
				BarbelMode.BITEMPORAL
						.managedBitemporalToCustomPersistenceObjects("some",
								BarbelTestHelper.generateJournalOfManagedDefaultPojos(BarbelHistoBuilder.barbel(), "some",
										Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 3, 1))))
						.size() == 2);
	}

	@Test
	public void testManagedBitemporalToCustomPersistenceObjectsTwoJournals_BitemporalMode() throws Exception {
		IndexedCollection<Object> bitemporals = BarbelTestHelper.generateJournalOfDefaultDocuments("some",
				Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 3, 1)));
		bitemporals.addAll(BarbelTestHelper.generateJournalOfDefaultDocuments("other",
				Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 3, 1))));
		assertTrue(BarbelMode.BITEMPORAL.managedBitemporalToCustomPersistenceObjects("some", bitemporals).size() == 2);
	}
	
	@Test
	public void testCustomPersistenceObjectsToManagedBitemporals_BitemporalMode() throws Exception {
		assertTrue(BarbelMode.BITEMPORAL.customPersistenceObjectsToManagedBitemporals(BarbelHistoBuilder.barbel(),
				Arrays.asList(BarbelTestHelper.random(BitemporalVersion.class),
						BarbelTestHelper.random(BitemporalVersion.class)))
				.size() == 2);
	}

	@Test
	public void testCustomPersistenceObjectsToManagedBitemporals_Wrongtype_BitemporalMode() throws Exception {
		assertTrue(BarbelMode.BITEMPORAL
				.customPersistenceObjectsToManagedBitemporals(BarbelHistoBuilder.barbel(), Arrays.asList(
						BarbelTestHelper.random(DefaultDocument.class), BarbelTestHelper.random(DefaultDocument.class)))
				.size() == 2);
	}

	@Test
	public void testCopyManagedBitemporal_BitemporalMode() throws Exception {
		Bitemporal managed = BarbelMode.BITEMPORAL.snapshotMaiden(BarbelHistoBuilder.barbel(),
				EnhancedRandom.random(DefaultDocument.class), BitemporalStamp.createActive());
		Bitemporal copy = BarbelMode.BITEMPORAL.copyManagedBitemporal(BarbelHistoBuilder.barbel(), managed);
		assertEquals(managed, copy);
		assertNotSame(managed, copy);
	}

	@Test
    	public void testValidateMaidenCandidate_BitemporalMode() throws Exception {
    		assertThrows(IllegalArgumentException.class, () -> BarbelMode.BITEMPORAL
    				.validateMaidenCandidate(BarbelHistoBuilder.barbel(), BarbelTestHelper.random(DefaultPojo.class)));
    	}

	@Test
    	public void testValidateMaidenCandidate_MissingDocId_BitemporalMode() throws Exception {
    		assertThrows(IllegalArgumentException.class, () -> BarbelMode.BITEMPORAL
    				.validateMaidenCandidate(BarbelHistoBuilder.barbel(), BarbelTestHelper.random(wodocid.class)));
    	}

	@Test
	public void testDrawMaiden_BitemporalMode() throws Exception {
		DefaultDocument pojo = EnhancedRandom.random(DefaultDocument.class);
		Object maiden = BarbelMode.BITEMPORAL.drawMaiden(BarbelHistoBuilder.barbel(), pojo);
		assertEquals(pojo, maiden);
	}

	@Test
	public void testDrawMaiden_Bitemporal() throws Exception {
		DefaultDocument pojo = EnhancedRandom.random(DefaultDocument.class);
		Bitemporal managed = BarbelMode.BITEMPORAL.snapshotMaiden(BarbelHistoBuilder.barbel(), pojo,
				BitemporalStamp.createActive());
		Object maiden = BarbelMode.BITEMPORAL.drawMaiden(BarbelHistoBuilder.barbel(), managed);
		assertNotEquals(pojo, maiden); // stamps differ
		assertEquals(managed, maiden);
		assertSame(managed, maiden);
	}

	@Test
	public void testGetPersistenceObjectType_BitemporalMode() throws Exception {
		assertEquals(BarbelMode.BITEMPORAL.getPersistenceObjectType(DefaultDocument.class), DefaultDocument.class);
	}

    @Test
    public void testManagedBitemporalToCustomPersistenceObject_BitemporalMode() throws Exception {
        DefaultDocument doc = new DefaultDocument(BitemporalStamp.createActive(), "some data");
        assertEquals(BarbelMode.BITEMPORAL.managedBitemporalToCustomPersistenceObject(doc), doc);
    }

}
