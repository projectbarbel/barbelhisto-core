package com.projectbarbel.histo.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;

public class EffectivePeriodTest {

    @Test
    public void testGetEffectiveFromInstant() throws Exception {
        LocalDate date = null;
        EffectivePeriod ep = EffectivePeriod.builder().from(date).build();
        assertEquals(BarbelHistoContext.CONTEXT.clock().now().toLocalDate(), ep.from());
    }

    @Test
    public void testGetEffectiveUntilInstant() throws Exception {
        LocalDate date = null;
        EffectivePeriod ep = EffectivePeriod.builder().until(date).build();
        assertEquals(BarbelHistoContext.CONTEXT.infiniteDate(), ep.until());
    }

}
