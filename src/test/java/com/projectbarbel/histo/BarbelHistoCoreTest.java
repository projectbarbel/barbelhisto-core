package com.projectbarbel.histo;

import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCoreTest {

//    @Test
    public void testSave() throws Exception {
        DefaultPojo somePojo = EnhancedRandom.random(DefaultPojo.class);
        BarbelHisto barbel = BarbelHistoBuilder.barbel().build();
//        barbel.save(barbel);
    }

}
