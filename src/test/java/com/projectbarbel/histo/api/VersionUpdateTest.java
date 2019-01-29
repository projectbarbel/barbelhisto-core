package com.projectbarbel.histo.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class VersionUpdateTest {

    @Test
    public void testOf() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdateResult result = VersionUpdate.of(object).execute();
        assertNotNull(result);
        assertNotNull(result.newPrecedingVersion());
        assertNotNull(result.newSubsequentVersion());
        assertNotNull(result.oldVersion());
        assertEquals(result.oldVersion(),object);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveUntil() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object).effectiveFrom(object.getEffectiveUntil()).execute();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveUntil_effectiveUntilIsInfinite() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object).effectiveFrom(LocalDate.MAX).execute();
    }

    @Test
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveFrom() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object).effectiveFrom(object.getEffectiveFrom()).execute();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_toHigh() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object).effectiveFrom(object.getEffectiveUntil().plusDays(1)).execute();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_toLow() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate.of(object).effectiveFrom(object.getEffectiveFrom().minusDays(1)).execute();
    }

    @Test
    public void testOf_bothNewVersionsMustBeActive() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdateResult update = VersionUpdate.of(object).effectiveFrom(validNewEffectiveDate(object)).execute();
        assertTrue(update.newPrecedingVersion().getBitemporalStamp().isActive());
        assertTrue(update.newSubsequentVersion().getBitemporalStamp().isActive());
        assertFalse(update.oldVersion().getBitemporalStamp().isActive());
    }

    private LocalDate validNewEffectiveDate(DefaultDocument object) {
        return BarbelTestHelper.randomLocalDate(object.getEffectiveFrom(), object.getEffectiveUntil());
    }

    @Test
    public void testOf_correctEffectiveFromDates() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult update = VersionUpdate.of(object).effectiveFrom(newEffectDate).execute();
        assertEquals(update.newPrecedingVersion().getEffectiveFrom(), object.getEffectiveFrom());
        assertEquals(update.newSubsequentVersion().getEffectiveFrom(), newEffectDate);
        assertEquals(update.oldVersion().getEffectiveFrom(), object.getEffectiveFrom());
    }

    @Test
    public void testOf_correctEffectiveUntilDates() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult update = VersionUpdate.of(object).effectiveFrom(newEffectDate).execute();
        assertEquals(update.newPrecedingVersion().getEffectiveUntil(), newEffectDate);
        assertEquals(update.newSubsequentVersion().getEffectiveUntil(), object.getEffectiveUntil());
        assertEquals(update.oldVersion().getEffectiveUntil(), object.getEffectiveUntil());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult update = VersionUpdate.of(object).effectiveFrom(newEffectDate).execute();
        assertEquals(update.newPrecedingVersion().getEffectiveUntil(), update.newSubsequentVersion().getEffectiveFrom());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_FiniteOrigin() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult update = VersionUpdate.of(object).effectiveFrom(newEffectDate).execute();
        assertEquals(update.newSubsequentVersion().getEffectiveUntil(), object.getEffectiveUntil());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_InfiniteOrigin() throws Exception {
        DefaultDocument object = DefaultDocument
                .builder().withVersionId("bla").withData("data").withBitemporalStamp(BitemporalStamp.of("JUNITTest",
                        "someId", EffectivePeriod.create().fromNow().toInfinite(), RecordPeriod.create("JUNITTest")))
                .build();
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult update = VersionUpdate.of(object).effectiveFrom(newEffectDate).execute();
        assertEquals(object.getEffectiveUntil(), EffectivePeriod.INFINITE);
        assertTrue(object.isEffectiveInfinitely());
        assertFalse(update.newPrecedingVersion().isEffectiveInfinitely());
        assertTrue(update.newSubsequentVersion().isEffectiveInfinitely());
        assertEquals(update.newSubsequentVersion().getEffectiveUntil(), object.getEffectiveUntil());
    }

}
