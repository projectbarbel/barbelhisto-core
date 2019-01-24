package com.projectbarbel.histo.model;

import org.junit.Test;

public class RecordPeriodTest {

    @Test(expected=NullPointerException.class)
    public void testCreateString() throws Exception {
        RecordPeriod.create(null);
    }

}
