package org.projectbarbel.histo.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BitemporalVersionTest {

    @Test
    public void testEquals() throws Exception {
        BitemporalStamp stamp = BitemporalStamp.createActive();
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalVersion version1 = new BitemporalVersion(stamp, pojo);
        BitemporalVersion version2 = new BitemporalVersion(stamp, pojo);
        assertTrue(version1.equals(version2));
        assertTrue(version1.equals(version1));
        assertFalse(version1.equals(null));
        assertFalse(version1.equals(new Object()));
        assertTrue(version1.hashCode() == version2.hashCode());
        assertFalse(version1 == version2);
    }

	@Test
	public void testToString() throws Exception {
		assertNotNull(new BitemporalVersion(BitemporalStamp.createActive(), new DefaultDocument()).toString());
	}

}
