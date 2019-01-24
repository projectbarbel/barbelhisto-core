package com.projectbarbel.histo;

import org.junit.Test;

public class BarbelHistoOptionsTest {

    @Test
    public void testValidate_DefaultConfig_shouldBeValid() {
        BarbelHistoOptions.ACTIVE_CONFIG.validate();
    }

    @Test(expected=IllegalStateException.class)
    public void testValidate_missingDaoClassName() {
        BarbelHistoOptions.builder().withDaoSupplierClassName("").build().validate();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testValidate_missingServiceClassName() {
        BarbelHistoOptions.builder().withServiceSupplierClassName("").build().validate();
    }
    
}
