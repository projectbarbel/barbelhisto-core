package com.projectbarbel.histo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelModeTest {

    @Test
    public void testGetIdValue() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        assertEquals(pojo.getDocumentId(), BarbelMode.getIdValue(pojo).get());
    }

}
