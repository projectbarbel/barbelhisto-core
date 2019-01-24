package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class VersionUpdateTest {

    @Test
    public void testOf() throws Exception {
        DefaultValueObject object = BarbelTestHelper.random(DefaultValueObject.class);
        VersionUpdate update = VersionUpdate.of(object, LocalDate.now(), "JUNITTEST", "JUNIT");
        assertNotNull(update);
        assertNotNull(update.precedingVersion());
        assertNotNull(update.subsequentVersion());
    }

    @Test
    public void testOf_bothNewVersionsActive() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testOf_correctEffectiveUntilDates() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testOf_correctEffectiveFromDates() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther() throws Exception {
        throw new RuntimeException("alles fälle durchtesten");
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_FiniteOrigin() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testOf_newEffectivePeriods_nextToEachOther_InfiniteOrigin() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
