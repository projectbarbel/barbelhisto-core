package com.projectbarbel.histo.functions.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.functions.update.DefaultPojoCopier;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;

public class DefaultPojoCopierTest {

    public static class APojo implements Bitemporal<String> {
        
        public BitemporalStamp stamp;
        public String one = "soso";
        public String two = null;
        public int count = 12;
        public double so = 300d;
        public Integer intGr = new Integer(8);
        public Serializable sabbel = new String();
        public Class<?> clazz = this.getClass();

        @Override
        public BitemporalStamp getBitemporalStamp() {
            return stamp;
        }

        @Override
        public void setBitemporalStamp(BitemporalStamp stamp) {
            this.stamp = stamp;
        }
        
    }
    
    @Test
    public void testFlatCopyWithNewStamp() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        DefaultDocument copied = new DefaultPojoCopier().flatCopyWithNewStamp(object, BarbelTestHelper.random(BitemporalStamp.class));
        assertNotNull(copied);
    }

    @Test
    public void testFlatCopyWithNewStampOnlyStampChanged() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        BitemporalStamp newstamp = BarbelTestHelper.random(BitemporalStamp.class);
        BitemporalStamp oldstamp = object.getBitemporalStamp();
        DefaultDocument copied = new DefaultPojoCopier().flatCopyWithNewStamp(object, newstamp);
        assertEquals(object.getData(), copied.getData());
        assertNotEquals(object.getBitemporalStamp(), copied.getBitemporalStamp());
        assertEquals(object.getBitemporalStamp(), oldstamp);
        assertEquals(copied.getBitemporalStamp(), newstamp);
    }

    @Test
    public void testFlatCopyWithNewStampOnlyStampChanged_Pojo() throws Exception {
        APojo object = BarbelTestHelper.random(APojo.class);
        BitemporalStamp newstamp = BarbelTestHelper.random(BitemporalStamp.class);
        BitemporalStamp oldstamp = object.getBitemporalStamp();
        APojo copied = new DefaultPojoCopier().flatCopyWithNewStamp(object, newstamp);
        assertEquals(object.clazz, copied.clazz);
        assertEquals(object.count, copied.count);
        assertEquals(object.intGr, copied.intGr);
        assertEquals(object.one, copied.one);
        assertEquals(object.sabbel, copied.sabbel);
        assertTrue(object.so==copied.so);
        assertNotEquals(object.getBitemporalStamp(), copied.getBitemporalStamp());
        assertEquals(object.getBitemporalStamp(), oldstamp);
        assertEquals(copied.getBitemporalStamp(), newstamp);
    }
    
    @Test
    public void testFlatCopyWithNewStampNotEqual() throws Exception {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        DefaultDocument copied = new DefaultPojoCopier().flatCopyWithNewStamp(object, BarbelTestHelper.random(BitemporalStamp.class));
        assertNotEquals(copied, object);
    }

}
