package com.projectbarbel.histo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BarbelHistoOptionsTest {

    @Test
    public void testAllSet() throws Exception {
        assertTrue(BarbelHistoOptions.builder().withDefaultValues().build().allSet());
    }

    @Test
    public void testAllSet_oneNull() throws Exception {
        assertFalse(BarbelHistoOptions.builder().withDefaultValues().withDaoClassName(null).build().allSet());
    }
    
    
}
