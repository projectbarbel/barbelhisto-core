package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotNull;

import java.util.function.Supplier;

import org.junit.Test;

public class DefaultIDGeneratorSupplierTest {

    @Test
    public void testGet() throws Exception {
        Supplier<String> supplier = new DefaultIDGeneratorSupplier().get();
        assertNotNull(supplier);
    }

}
