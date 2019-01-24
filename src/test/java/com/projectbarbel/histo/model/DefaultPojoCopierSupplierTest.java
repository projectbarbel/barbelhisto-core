package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class DefaultPojoCopierSupplierTest {

    @Test
    public void testFlatCopyWithNewStamp() throws Exception {
        DefaultValueObject object = BarbelTestHelper.random(DefaultValueObject.class);
        DefaultValueObject copied = DefaultPojoCopierSupplier.flatCopyWithNewStamp(object, BarbelTestHelper.random(BitemporalStamp.class));
        assertNotNull(copied);
    }

    @Test
    public void testFlatCopyWithNewStampOnlyStampChanged() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testFlatCopyWithNewStampNotEqual() throws Exception {
        DefaultValueObject object = BarbelTestHelper.random(DefaultValueObject.class);
        DefaultValueObject copied = DefaultPojoCopierSupplier.flatCopyWithNewStamp(object, BarbelTestHelper.random(BitemporalStamp.class));
        assertNotEquals(copied, object);
    }

}
