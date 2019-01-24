package com.projectbarbel.histo.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class JournalTest {

    @Test
    public void testInstanceByList() {
        Journal<Bitemporal<String>, String> journal = Journal.instanceByList(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345", Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))));
        assertTrue(journal.size()==2);
    }

    @Test
    public void testSortByEffectiveDate() throws Exception {
        Journal<Bitemporal<String>, String> journal = Journal.instanceByList(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345", Arrays.asList(LocalDate.of(2019, 8, 1), LocalDate.of(2019, 4, 1), LocalDate.of(2019, 1, 1))));
        assertTrue(journal.list().get(1).getEffectiveFromIntant().isAfter(journal.list().get(2).getEffectiveFromIntant()));
        assertTrue(journal.list().get(0).getEffectiveFromIntant().equals(LocalDate.of(2019, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(journal.list().get(1).getEffectiveFromIntant().equals(LocalDate.of(2019, 4, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(journal.list().get(2).getEffectiveFromIntant().equals(LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertFalse(journal.sortByEffectiveDate().list().get(1).getEffectiveFromIntant().isAfter(journal.list().get(2).getEffectiveFromIntant()));
        assertTrue(journal.list().get(0).getEffectiveFromIntant().equals(LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(journal.list().get(1).getEffectiveFromIntant().equals(LocalDate.of(2019, 4, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(journal.list().get(2).getEffectiveFromIntant().equals(LocalDate.of(2019, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

}
