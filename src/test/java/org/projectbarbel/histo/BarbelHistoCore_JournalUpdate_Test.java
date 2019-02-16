package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.functions.DefaultJournalUpdateStrategy;
import org.projectbarbel.histo.functions.DefaultJournalUpdateStrategy.JournalUpdateCase;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;

public class BarbelHistoCore_JournalUpdate_Test {

	private DocumentJournal journal;
	private BarbelHistoContext context;

	@AfterAll
	public static void setup() {
		BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
	}

	@Test
	public void testApply_wrongId() throws Exception {
		DefaultDocument doc = new DefaultDocument();
		BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL);
		Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc, BitemporalStamp.createActive());
		journal = DocumentJournal
				.create(ProcessingState.INTERNAL, context,
						BarbelTestHelper.generateJournalOfDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
								LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
						"someId");
		assertThrows(IllegalArgumentException.class,
				() -> new DefaultJournalUpdateStrategy(context).accept(journal, bitemporal));
	}

	// @formatter:off
	@SuppressWarnings("unused")
	private static Stream<Arguments> createJournalUpdateCases() {

		// 1.1.2016 1.1.2017 1.1.2018 1.1.2019
		// |------------------|------------------|------------------|------| 30.1.2019
		// 10:00 Uhr (now)

		return Stream.of(
				Arguments.of(LocalDate.of(2015, 7, 1), LocalDate.of(2016, 7, 1), 2, JournalUpdateCase.PREOVERLAPPING,
						Arrays.asList(
								LocalDate.of(2015, 7, 1), LocalDate.of(2016, 7, 1), LocalDate.of(2016, 7, 1),
								LocalDate.of(2017, 1, 1)),
						1, Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1))),
				Arguments.of(LocalDate.of(2018, 7, 1), LocalDate.of(2018, 10, 1), 3, JournalUpdateCase.EMBEDDEDINTERVAL,
						Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 7, 1), LocalDate.of(2018, 7, 1),
								LocalDate.of(2018, 10, 1), LocalDate.of(2018, 10, 1), LocalDate.of(2019, 1, 1)),
						1, Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
				Arguments.of(LocalDate.of(2016, 7, 1), LocalDate.of(2018, 7, 1), 3, JournalUpdateCase.EMBEDDEDOVERLAY,
						Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 7, 1), LocalDate.of(2016, 7, 1),
								LocalDate.of(2018, 7, 1), LocalDate.of(2018, 7, 1), LocalDate.of(2019, 1, 1)),
						3,
						Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1),
								LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
				Arguments
						.of(LocalDate.of(2019, 1, 30), LocalDate.MAX, 2, JournalUpdateCase.POSTOVERLAPPING,
								Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 30),
										LocalDate.of(2019, 1, 30), LocalDate.MAX),
								1, Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.MAX)));
	}
	// @formatter:on

	@ParameterizedTest
	@MethodSource("createJournalUpdateCases")
	public void testSave_Pojo(LocalDate updateFrom, LocalDate updateUntil, int countOfNewVersions,
			JournalUpdateCase updateCase, List<LocalDate> activeEffective, int inactiveCount,
			List<LocalDate> inactiveEffective) throws Exception {
		BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
		BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO).withUser("testUser")
				.withBackboneSupplier(() -> BarbelTestHelper.generateJournalOfDefaultPojos("someId",
						Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1),
								LocalDate.of(2019, 1, 1))));
		BarbelHisto<DefaultPojo> core = ((BarbelHistoBuilder) context).build();
		DefaultPojo update = new DefaultPojo();
		update.setDocumentId("someId");
		update.setData("some data");
		core.save(update, updateFrom, updateUntil);
		journal = ((BarbelHistoCore<DefaultPojo>) core).getDocumentJournal("someId");
		assertEquals(countOfNewVersions, ((BarbelHistoCore<DefaultPojo>) core).getLastUpdate().newVersions.size());
		assertEquals(updateCase, ((BarbelHistoCore<DefaultPojo>) core).getLastUpdate().updateCase);
		assertNewVersions(((BarbelHistoCore<DefaultPojo>) core).getLastUpdate().requestedUpdate,
				((BarbelHistoCore<DefaultPojo>) core).getLastUpdate().newVersions, activeEffective);
		assertInactivatedVersions(inactiveCount, inactiveEffective);
	}

	@ParameterizedTest
	@MethodSource("createJournalUpdateCases")
	public void testAccept_Pojo(LocalDate updateFrom, LocalDate updateUntil, int countOfNewVersions,
			JournalUpdateCase updateCase, List<LocalDate> activeEffective, int inactiveCount,
			List<LocalDate> inactiveEffective) throws Exception {
		BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
		context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO).withUser("testUser");
		journal = DocumentJournal
				.create(ProcessingState.INTERNAL, context,
						BarbelTestHelper.generateJournalOfDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
								LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
						"someId");
		UpdateReturn updatReturn = performUpdate_Pojo(updateFrom, updateUntil);
		assertTrue(updatReturn.newVersions.size() == countOfNewVersions);
		assertEquals(updateCase, updatReturn.function.getActualCase());
		assertNewVersions(updatReturn.bitemporal, updatReturn.newVersions, activeEffective);
		assertInactivatedVersions(inactiveCount, inactiveEffective);
	}

	private void assertInactivatedVersions(int inactiveCount, List<LocalDate> inactiveEffective) {
		List<Bitemporal> inactivated = journal.read().inactiveVersions();
		assertEquals(inactiveCount, inactivated.size());
		for (int i = 0; i < inactivated.size(); i++) {
			assertInactivatedVersion(inactivated.get(i), inactiveEffective.get(i * 2),
					inactiveEffective.get(i * 2 + 1));
		}
	}

	private UpdateReturn performUpdate_Pojo(LocalDate from, LocalDate until) {
		DefaultPojo update = new DefaultPojo();
		update.setDocumentId("someId");
		update.setData("some data");
		Bitemporal bitemporal = context.getMode().snapshotMaiden(context, update,
				BitemporalStamp.createActive(context, "someId", EffectivePeriod.of(from, until)));
		DefaultJournalUpdateStrategy updateStrategy = new DefaultJournalUpdateStrategy(context);
		updateStrategy.accept(journal, bitemporal);
		return new UpdateReturn(journal.getLastInsert(), bitemporal, updateStrategy);
	}

	@ParameterizedTest
	@MethodSource("createJournalUpdateCases")
	public void testAccept_Bitemporal(LocalDate updateFrom, LocalDate updateUntil, int countOfNewVersions,
			JournalUpdateCase updateCase, List<LocalDate> activeEffective, int inactiveCount,
			List<LocalDate> inactiveEffective) throws Exception {
		BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
		context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL).withUser("testUser");
		journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
				BarbelTestHelper.generateJournalOfDefaultDocuments("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
						LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
				"someId");
		UpdateReturn updatReturn = performUpdate_Bitemporal(updateFrom, updateUntil);
		assertTrue(updatReturn.newVersions.size() == countOfNewVersions);
		assertEquals(updateCase, updatReturn.function.getActualCase());
		assertNewVersions(updatReturn.bitemporal, updatReturn.newVersions, activeEffective);
		assertInactivatedVersions(inactiveCount, inactiveEffective);
	}

	private UpdateReturn performUpdate_Bitemporal(LocalDate from, LocalDate until) {
		DefaultDocument doc = new DefaultDocument();
		Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc,
				BitemporalStamp.createActive(context, "someId", EffectivePeriod.of(from, until)));

		DefaultJournalUpdateStrategy function = new DefaultJournalUpdateStrategy(context);
		function.accept(journal, bitemporal);
		List<Bitemporal> list = journal.getLastInsert();
		return new UpdateReturn(list, bitemporal, function);
	}

	private void assertNewVersions(Bitemporal insertedBitemporal, List<Bitemporal> newVersions,
			List<LocalDate> activeEffective) {

		for (int i = 0; i < newVersions.size(); i++) {
			assertEquals(newVersions.get(i).getBitemporalStamp().getEffectiveTime().from(), activeEffective.get(i * 2));
			assertEquals(newVersions.get(i).getBitemporalStamp().getEffectiveTime().until(),
					activeEffective.get(i * 2 + 1));
			assertEquals(newVersions.get(i).getBitemporalStamp().getRecordTime().getCreatedAt(),
					ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
			assertEquals(newVersions.get(i).getBitemporalStamp().getRecordTime().getCreatedBy(), "testUser");
			assertEquals(newVersions.get(i).getBitemporalStamp().getRecordTime().getInactivatedAt(),
					RecordPeriod.NOT_INACTIVATED);
			assertEquals(newVersions.get(i).getBitemporalStamp().getRecordTime().getInactivatedBy(),
					RecordPeriod.NOBODY);
			assertEquals(newVersions.get(i).getBitemporalStamp().getRecordTime().getState(),
					BitemporalObjectState.ACTIVE);
		}

	}

	private void assertInactivatedVersion(Bitemporal inactivated, LocalDate from, LocalDate until) {

		assertEquals(inactivated.getBitemporalStamp().getEffectiveTime().from(), from);
		assertEquals(inactivated.getBitemporalStamp().getEffectiveTime().until(), until);

		assertEquals(inactivated.getBitemporalStamp().getRecordTime().getCreatedAt(),
				ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
		assertEquals(inactivated.getBitemporalStamp().getRecordTime().getCreatedBy(), "SYSTEM");
		assertEquals(inactivated.getBitemporalStamp().getRecordTime().getInactivatedAt(),
				ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
		assertEquals(inactivated.getBitemporalStamp().getRecordTime().getInactivatedBy(), "testUser");
		assertEquals(inactivated.getBitemporalStamp().getRecordTime().getState(), BitemporalObjectState.INACTIVE);

	}

	private static class UpdateReturn {
		public List<Bitemporal> newVersions;
		public Bitemporal bitemporal;
		public DefaultJournalUpdateStrategy function;

		public UpdateReturn(List<Bitemporal> newVersions, Bitemporal bitemporal,
				DefaultJournalUpdateStrategy function) {
			super();
			this.newVersions = newVersions;
			this.bitemporal = bitemporal;
			this.function = function;
		}
	}

}
