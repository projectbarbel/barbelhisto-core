package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class VersionUpdateTest {

    @Test
    public void testOf() throws Exception {
        DefaultValueObject object = BarbelTestHelper.random(DefaultValueObject.class);
        VersionUpdate<DefaultValueObject> update = VersionUpdate.of(object, LocalDate.now(), "JUNITTEST", "JUNIT");
        assertNotNull(update);
        assertNotNull(update.precedingVersion());
        assertNotNull(update.subsequentVersion());
    }

}
