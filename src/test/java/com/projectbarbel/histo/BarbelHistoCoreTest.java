package com.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCoreTest {

    @Test
    public void testSave() throws Exception {
        BarbelHisto<String> core = BarbelHistoBuilder.barbel().build();
        assertThrows(IllegalArgumentException.class, ()->core.save("some", LocalDate.now(), LocalDate.MAX));
    }

    @Test
    public void testSave_LocalDatesInvalid() throws Exception {
        BarbelHisto<DefaultPojo> core = BarbelHistoBuilder.barbel().build();
        assertThrows(IllegalArgumentException.class, ()->core.save(EnhancedRandom.random(DefaultPojo.class), LocalDate.MAX, LocalDate.now()));
    }
    
}
