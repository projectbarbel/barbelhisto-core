package com.projectbarbel.histo.model;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.Test;

public class EffectivePeriodTest {

    @Test(expected=NullPointerException.class)
        public void testGetEffectiveFromInstant() throws Exception {
            Instant instant = null;
            EffectivePeriod.create().from(instant);
        }

    @Test(expected=NullPointerException.class)
                    public void testGetEffectiveFromLocalDate() throws Exception {
                        LocalDate date = null;
                        EffectivePeriod.create().from(date);
                    }

    @Test(expected=NullPointerException.class)
        public void testGetEffectiveUntilLocalDate() throws Exception {
            LocalDate date = null;
            EffectivePeriod.create().until(date);
        }

    @Test(expected=NullPointerException.class)
        public void testGetEffectiveUntilInstant() throws Exception {
            Instant date = null;
            EffectivePeriod.create().until(date);
        }

}
