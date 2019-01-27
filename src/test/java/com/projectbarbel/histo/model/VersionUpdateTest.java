package com.projectbarbel.histo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class VersionUpdateTest {

    @Test
    public void testOf() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(object.getEffectiveFrom().plusDays(1)).execute();
        assertNotNull(update);
        assertNotNull(update.precedingVersion());
        assertNotNull(update.subsequentVersion());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveUntil() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(object.getEffectiveUntil()).execute();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveUntil_effectiveUntilIsInfinite() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(LocalDate.MAX).execute();
    }

    @Test
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveFrom() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(object.getEffectiveFrom()).execute();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_toHigh() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(object.getEffectiveUntil().plusDays(1)).execute();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_toLow() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(object.getEffectiveFrom().minusDays(1)).execute();
    }

    @Test
    public void testOf_bothNewVersionsMustBeActive() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(validNewEffectiveDate(object)).execute();
        assertTrue(update.precedingVersion().getBitemporalStamp().isActive());
        assertTrue(update.subsequentVersion().getBitemporalStamp().isActive());
    }

    private LocalDate validNewEffectiveDate(DefaultDocument object) {
        return BarbelTestHelper.randomLocalDate(object.getEffectiveFrom(), object.getEffectiveUntil());
    }

    @Test
    public void testOf_correctEffectiveFromDates() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(newEffectDate).execute();
        assertEquals(update.precedingVersion().getEffectiveFrom(), object.getEffectiveFrom());
        assertEquals(update.subsequentVersion().getEffectiveFrom(), newEffectDate);
    }

    @Test
    public void testOf_correctEffectiveUntilDates() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(newEffectDate).execute();
        assertEquals(update.precedingVersion().getEffectiveUntil(), newEffectDate);
        assertEquals(update.subsequentVersion().getEffectiveUntil(), object.getEffectiveUntil());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(newEffectDate).execute();
        assertEquals(update.precedingVersion().getEffectiveUntil(), update.subsequentVersion().getEffectiveFrom());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_FiniteOrigin() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(newEffectDate).execute();
        assertEquals(update.subsequentVersion().getEffectiveUntil(), object.getEffectiveUntil());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_InfiniteOrigin() throws Exception {
        DefaultDocument object = DefaultDocument
                .builder().withVersionId("bla").withData("data").withBitemporalStamp(BitemporalStamp.of("JUNITTest",
                        "someId", EffectivePeriod.create().fromNow().toInfinite(), RecordPeriod.create("JUNITTest")))
                .build();
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdate update = VersionUpdate.of(object, new DefaultPojoCopier()).effectiveFrom(newEffectDate).execute();
        assertEquals(object.getEffectiveUntilInstant(), EffectivePeriod.INFINITE);
        assertTrue(object.isEffectiveInfinitely());
        assertFalse(update.precedingVersion().isEffectiveInfinitely());
        assertTrue(update.subsequentVersion().isEffectiveInfinitely());
        assertEquals(update.subsequentVersion().getEffectiveUntil(), object.getEffectiveUntil());
    }

}
