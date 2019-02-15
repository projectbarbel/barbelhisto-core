package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelModeTest {

    @Test
    public void testGetIdValue() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        assertEquals(pojo.getDocumentId(), BarbelMode.getIdValue(pojo).get());
    }

}
