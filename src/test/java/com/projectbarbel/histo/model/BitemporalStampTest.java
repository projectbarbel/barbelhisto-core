package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class BitemporalStampTest {

    @Test()
    public void testArgumentValidationInConstructor_Valid() {
        BitemporalStamp stamp = new BitemporalStamp(BarbelTestHelper.random(BitemporalStamp.class));
        assertNotNull(stamp);
    }
    
    @Test
    public void testArgumentValidationInConstructor_ValidCheckAllNotNull() throws Exception {
        BitemporalStamp stamp = new BitemporalStamp(BarbelTestHelper.random(BitemporalStamp.class));
        assertNotNull(stamp.getVersionId());
        assertNotNull(stamp.getEffectiveTime());
        assertNotNull(stamp.getRecordTime());
        assertNotNull(stamp.getActivity());
    }

    @Test(expected=NullPointerException.class)
    public void testArgumentValidationInConstructor_documentId () {
        new BitemporalStamp(BarbelTestHelper.random(BitemporalStamp.class, "documentId"));
    }

    @Test(expected=NullPointerException.class)
    public void testArgumentValidationInConstructor_EffectiveTime() throws Exception {
        new BitemporalStamp(BarbelTestHelper.random(BitemporalStamp.class, "effectiveTime"));
    }

    @Test(expected=NullPointerException.class)
    public void testBitemporalStampBitemporalStamp() throws Exception {
        new BitemporalStamp(BarbelTestHelper.random(BitemporalStamp.class, "recordTime"));
    }

    @Test(expected=NullPointerException.class)
    public void testBitemporalStampBitemporalStampActivity() throws Exception {
        new BitemporalStamp(BarbelTestHelper.random(BitemporalStamp.class, "activity"));
    }
    
}
