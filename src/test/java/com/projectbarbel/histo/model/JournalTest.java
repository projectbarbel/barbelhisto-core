package com.projectbarbel.histo.model;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class JournalTest {

    @Test
    public void testInstanceByList() {
        Journal<Bitemporal<String>, String> journal = Journal.instanceByList(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345", Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))));
        assertTrue(journal.size()==2);
    }

}
