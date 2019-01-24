package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BitemporalTest implements Bitemporal<String> {

    private BitemporalStamp bitemporalStamp;
    @SuppressWarnings("unused")
    private String data = "some data";
    private String versionId = UUID.randomUUID().toString();;

    @Test
    public void testNewVersion() throws Exception {
        Bitemporal<String> bitemporalTest = EnhancedRandom.random(BitemporalTest.class);
        BitemporalTest bitemporal = bitemporalTest.newVersion("TEST", "JUNI", EffectivePeriod.instance().fromNow().toInfinite());
        assertNotNull(bitemporal);
    }

    @Test
    public void testNewVersionNotEqualOrigin() throws Exception {
        Bitemporal<String> bitemporalTest = EnhancedRandom.random(BitemporalTest.class);
        BitemporalTest bitemporal = bitemporalTest.newVersion("TEST", "JUNI", EffectivePeriod.instance().fromNow().toInfinite());
        assertNotEquals(this, bitemporal);
    }

    @Test
    public void testNewVersionNotEqualOriginBitemporalStampNotNull() throws Exception {
        Bitemporal<String> bitemporalTest = EnhancedRandom.random(BitemporalTest.class);
        BitemporalTest bitemporal = bitemporalTest.newVersion("TEST", "JUNI", EffectivePeriod.instance().fromNow().toInfinite());
        assertNotNull(bitemporal.getBitemporalStamp());
    }

    @Test
    public void testNewVersionNotEqualOriginContainsNewStamp() throws Exception {
        Bitemporal<String> bitemporalTest = EnhancedRandom.random(BitemporalTest.class);
        BitemporalTest bitemporal = bitemporalTest.newVersion("TEST", "JUNI", EffectivePeriod.instance().fromNow().toInfinite());
        assertNotEquals(bitemporalTest.getBitemporalStamp(), bitemporal.getBitemporalStamp());
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return bitemporalStamp;
    }

    @Override
    public String getVersionId() {
        return versionId ;
    }

    @Test
    public void testFlatCopyWithNewStamp() throws Exception {
        fail("fehlen diverse tests");
    }

}
