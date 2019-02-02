package com.projectbarbel.histo.functions.journal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class JournalPredicatesTest {

    @Test
    public void testIsActive() throws Exception {
        DefaultDocument doc = BarbelTestHelper.random(DefaultDocument.class);
        assertTrue(new JournalPredicates().isActive(doc));
    }

    @Test
    public void testEffectiveOn_dueDateOnEffectiveFrom() throws Exception {
        DefaultDocument doc = BarbelTestHelper.random(DefaultDocument.class);
        assertTrue(new JournalPredicates(doc.getEffectiveFrom()).effectiveOn(doc));
    }

    @Test
    public void testEffectiveOn_dueDateAfterEffectivePeriod() throws Exception {
        DefaultDocument doc = BarbelTestHelper.random(DefaultDocument.class);
        assertFalse(new JournalPredicates(doc.getEffectiveUntil().plusDays(1)).effectiveOn(doc));
    }
    
    @Test
    public void testEffectiveOn_dueDateOneDayBeforeEffectiveUntil() throws Exception {
        DefaultDocument doc = BarbelTestHelper.random(DefaultDocument.class);
        assertTrue(new JournalPredicates(doc.getEffectiveUntil().minusDays(1)).effectiveOn(doc));
    }

    @Test
    public void testEffectiveOn_dueDateOnEffectiveUntil() throws Exception {
        DefaultDocument doc = BarbelTestHelper.random(DefaultDocument.class);
        assertFalse(new JournalPredicates(doc.getEffectiveUntil()).effectiveOn(doc));
    }
    
    @Test
    public void testEffectiveOn_dueDateBeforeEffectiveFrom() throws Exception {
        DefaultDocument doc = BarbelTestHelper.random(DefaultDocument.class);
        assertFalse(new JournalPredicates(doc.getEffectiveFrom().minusDays(1)).effectiveOn(doc));
    }

    @Test
    public void testEffectiveAfter_isAfter_dueDateBeforeEffectivePeriod() throws Exception {
        DefaultDocument doc = DefaultDocument.builder().withData("bla").withBitemporalStamp(BitemporalStamp.of("test", "docid", EffectivePeriod.builder().from(LocalDate.now().plusDays(1)).toInfinite().build(), RecordPeriod.builder().build())).build();
        assertTrue(new JournalPredicates(LocalDate.now()).effectiveAfter(doc));
    }

    @Test
    public void testEffectiveAfter_isAfter_dueDateOnEffectiveFrom() throws Exception {
        DefaultDocument doc = DefaultDocument.builder().withData("bla").withBitemporalStamp(BitemporalStamp.of("test", "docid", EffectivePeriod.builder().from(LocalDate.now()).toInfinite().build(), RecordPeriod.builder().build())).build();
        assertTrue(new JournalPredicates(LocalDate.now()).effectiveAfter(doc));
    }

    @Test
    public void testEffectiveAfter_notAfter_dueDateInEffectivePeriod() throws Exception {
        DefaultDocument doc = DefaultDocument.builder().withData("bla").withBitemporalStamp(BitemporalStamp.of("test", "docid", EffectivePeriod.builder().from(LocalDate.now().minusDays(1)).toInfinite().build(), RecordPeriod.builder().build())).build();
        assertFalse(new JournalPredicates(LocalDate.now()).effectiveAfter(doc));
    }

    @Test
    public void testEffectiveAfter_notAfter_dueDateAfterEffectivePeriod() throws Exception {
        DefaultDocument doc = DefaultDocument.builder().withData("bla").withBitemporalStamp(BitemporalStamp.of("test", "docid", EffectivePeriod.builder().from(LocalDate.now().minusDays(10)).until(LocalDate.now().minusDays(1)).build(), RecordPeriod.builder().build())).build();
        assertFalse(new JournalPredicates(LocalDate.now()).effectiveAfter(doc));
    }

    @Test
    public void testEffectiveAfter_notAfter_dueDateOnEffectiveUntil() throws Exception {
        DefaultDocument doc = DefaultDocument.builder().withData("bla").withBitemporalStamp(BitemporalStamp.of("test", "docid", EffectivePeriod.builder().from(LocalDate.now().minusDays(10)).until(LocalDate.now()).build(), RecordPeriod.builder().build())).build();
        assertFalse(new JournalPredicates(LocalDate.now()).effectiveAfter(doc));
    }

}
