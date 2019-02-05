package com.projectbarbel.histo;

import org.junit.Test;

import com.projectbarbel.histo.model.DefaultPojo;

public class BarbelHistoBuilderTest {

    @Test
    public void testBarbel_testAssignment() throws Exception {
        @SuppressWarnings("unused")
        BarbelHisto<DefaultPojo> barbel = BarbelHistoBuilder.barbel().build();
    }

}
