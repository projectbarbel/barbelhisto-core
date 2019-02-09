package com.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelModeTest {

    @Test
    public void testGetIdValue() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        assertEquals(pojo.getDocumentId(), BarbelMode.getIdValue(pojo).get());
    }

}
