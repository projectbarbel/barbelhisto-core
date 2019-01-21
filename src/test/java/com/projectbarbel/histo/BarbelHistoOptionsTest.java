package com.projectbarbel.histo;

import org.junit.Test;

public class BarbelHistoOptionsTest {

    @Test
    public void testValidate_DefaultConfig_shouldBeValid() {
        BarbelHistoOptions.DEFAULT_CONFIG.validate();
    }

    @Test(expected=IllegalStateException.class)
    public void testValidate_missingDaoClassName() {
        BarbelHistoOptions.builder().withDaoClassName("").build().validate();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testValidate_missingServiceClassName() {
        BarbelHistoOptions.builder().withServiceClassName("").build().validate();
    }
    
}
