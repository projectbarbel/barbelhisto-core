package com.projectbarbel.histo.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class BitemporalStampTest {

    @Test
    public void testCopy() throws Exception {
        BitemporalStamp bs = BitemporalStamp.of("test", "test", EffectivePeriod.of(LocalDate.now(), LocalDate.MAX),
                RecordPeriod.builder().build());
        assertNotNull(bs);
    }

    @Test
    public void testCopy_versionIdSet() throws Exception {
        BitemporalStamp bs = BitemporalStamp.of("test", "test", EffectivePeriod.of(LocalDate.now(), LocalDate.MAX),
                RecordPeriod.builder().build());
        assertNotNull(bs.getVersionId());
    }

}
