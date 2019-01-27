package com.projectbarbel.histo;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BarbelHistoContextTest {

    @Test
    public void testCreateDefault() throws Exception {
        BarbelHistoContext ctx = BarbelHistoContext.createDefault();
        assertNotNull(ctx);
    }

    @Test
    public void testCreateDefault_withDefaultFactory() throws Exception {
        BarbelHistoContext ctx = BarbelHistoContext.createDefault();
        assertNotNull(ctx.factory());
    }

    @Test
    public void testCreateDefault_withDefaultOptions() throws Exception {
        BarbelHistoContext ctx = BarbelHistoContext.createDefault();
        assertNotNull(ctx.options());
    }

    @Test
    public void testOf() throws Exception {
        BarbelHistoContext ctx = BarbelHistoContext.of(BarbelHistoOptions.withDefaultValues(), BarbelHistoFactory.withDefaultValues());
        assertNotNull(ctx);
    }

    @Test
    public void testOf_containsFactory() throws Exception {
        BarbelHistoContext ctx = BarbelHistoContext.of(BarbelHistoOptions.withDefaultValues(), BarbelHistoFactory.withDefaultValues());
        assertNotNull(ctx.factory());
    }

    @Test
    public void testOf_containsOptions() throws Exception {
        BarbelHistoContext ctx = BarbelHistoContext.of(BarbelHistoOptions.withDefaultValues(), BarbelHistoFactory.withDefaultValues());
        assertNotNull(ctx.options());
    }

    
}
