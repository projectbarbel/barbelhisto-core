package com.projectbarbel.histo.journal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.journal.VersionUpdate;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateExecutionBuilder;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class VersionUpdateTest {

    @Test
    public void testOf() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdateResult<DefaultDocument> result = BarbelHistoFactory.createDefaultVersionUpdate(object).execute();
        assertNotNull(result);
        assertNotNull(result.newPrecedingVersion());
        assertNotNull(result.newSubsequentVersion());
        assertNotNull(result.oldVersion());
        assertEquals(result.oldVersion(), object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveUntil() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdate<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object);
        update.prepare().effectiveFrom(object.getBitemporalStamp().getEffectiveTime().until());
        update.execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveUntil_effectiveUntilIsInfinite()
            throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(LocalDate.MAX).execute();
    }

    @Test
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_onOriginEffectiveFrom() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(object.getBitemporalStamp().getEffectiveTime().from()).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_toHigh() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(object.getBitemporalStamp().getEffectiveTime().until().plusDays(1)).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_effectiveDateOfNewVersionMustBeWithinBoundaries_toLow() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(object.getBitemporalStamp().getEffectiveTime().from().minusDays(1)).execute();
    }

    @Test
    public void testOf_bothNewVersionsMustBeActive() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        VersionUpdateResult<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare()
                .effectiveFrom(validNewEffectiveDate(object)).execute();
        assertTrue(update.newPrecedingVersion().getBitemporalStamp().isActive());
        assertTrue(update.newSubsequentVersion().getBitemporalStamp().isActive());
        assert (update.oldVersion().getBitemporalStamp().isActive());
    }

    private LocalDate validNewEffectiveDate(DefaultDocument object) {
        return BarbelTestHelper.randomLocalDate(object.getBitemporalStamp().getEffectiveTime().from(), object.getBitemporalStamp().getEffectiveTime().until());
    }

    @Test
    public void testOf_correctEffectiveFromDates() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(newEffectDate)
                .execute();
        assertEquals(update.newPrecedingVersion().getBitemporalStamp().getEffectiveTime().from(), object.getBitemporalStamp().getEffectiveTime().from());
        assertEquals(update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime().from(), newEffectDate);
        assertEquals(update.oldVersion().getBitemporalStamp().getEffectiveTime().from(), object.getBitemporalStamp().getEffectiveTime().from());
    }

    @Test(expected = IllegalStateException.class)
    public void testOf_correctEffectiveFromDates_twoExecutesShouldCauseError() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateExecutionBuilder<DefaultDocument> builder = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare()
                .effectiveFrom(newEffectDate);
        builder.execute();
        builder.execute();
    }

    @Test
    public void testOf_correctEffectiveUntilDates() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(newEffectDate)
                .execute();
        assertEquals(update.newPrecedingVersion().getBitemporalStamp().getEffectiveTime().until(), newEffectDate);
        assertEquals(update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime().until(), object.getBitemporalStamp().getEffectiveTime().until());
        assertEquals(update.oldVersion().getBitemporalStamp().getEffectiveTime().until(), object.getBitemporalStamp().getEffectiveTime().until());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(newEffectDate)
                .execute();
        assertEquals(update.newPrecedingVersion().getBitemporalStamp().getEffectiveTime().until(),
                update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_FiniteOrigin() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(newEffectDate)
                .execute();
        assertEquals(update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime().until(), object.getBitemporalStamp().getEffectiveTime().until());
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_InfiniteOrigin() throws Exception {
        DefaultDocument object = DefaultDocument.builder().withData("data")
                .withBitemporalStamp(BitemporalStamp.of("JUNITTest", "someId",
                        EffectivePeriod.builder().fromNow().toInfinite().build(), RecordPeriod.builder().build()))
                .build();
        LocalDate newEffectDate = validNewEffectiveDate(object);
        VersionUpdateResult<DefaultDocument> update = BarbelHistoFactory.createDefaultVersionUpdate(object).prepare().effectiveFrom(newEffectDate)
                .execute();
        assertEquals(object.getBitemporalStamp().getEffectiveTime().until(), BarbelHistoContext.getInfiniteDate());
        assertTrue(object.getBitemporalStamp().getEffectiveTime().isInfinite());
        assertFalse(update.newPrecedingVersion().getBitemporalStamp().getEffectiveTime().isInfinite());
        assertTrue(update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals(update.newSubsequentVersion().getBitemporalStamp().getEffectiveTime().until(), object.getBitemporalStamp().getEffectiveTime().until());
    }

    public static class DefaultDocumentExt extends DefaultDocument {
        private DefaultDocument document = new DefaultDocument();

        public DefaultDocumentExt() {
            super();
        }

        public DefaultDocument getDocument() {
            return document;
        }

        public void setDocument(DefaultDocument document) {
            this.document = document;
        }

        public DefaultDocumentExt(DefaultDocument document) {
            super();
            this.document = document;
            super.setBitemporalStamp(document.getBitemporalStamp());
        }
    }

}
