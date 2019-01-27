package com.projectbarbel.histo.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class JournalTest {

    @Test
        public void testCreate() {
            Journal<Bitemporal<String>, String> journal = Journal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345", Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))));
            assertTrue(journal.size()==2);
        }

    @Test
    public void testSortByEffectiveDate() throws Exception {
        Journal<Bitemporal<String>, String> journal = Journal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345", Arrays.asList(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 8, 1), LocalDate.of(2019, 1, 1))));
        assertFalse(journal.sortByEffectiveDate().list().get(1).getEffectiveFromInstant().isAfter(journal.list().get(2).getEffectiveFromInstant()));
        assertTrue(journal.list().get(0).getEffectiveFromInstant().equals(LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(journal.list().get(1).getEffectiveFromInstant().equals(LocalDate.of(2019, 4, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(journal.list().get(2).getEffectiveFromInstant().equals(LocalDate.of(2019, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

    @Test
    public void testPrettyPrint() throws Exception {
        Journal.create(Collections.emptyList()).prettyPrint();
    }

}
