package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BitemporalStampTest {

    @Test
        public void testCopy() throws Exception {
            BitemporalStamp bs = BitemporalStamp.of("test", "test", EffectivePeriod.builder().build(), RecordPeriod.builder().build());
            assertNotNull(bs);
        }

    @Test
        public void testCopy_versionIdSet() throws Exception {
            BitemporalStamp bs = BitemporalStamp.of("test", "test", EffectivePeriod.builder().build(), RecordPeriod.builder().build());
            assertNotNull(bs.getVersionId());
        }

    
}
