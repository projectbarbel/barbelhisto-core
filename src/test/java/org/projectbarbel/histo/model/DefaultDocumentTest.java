package org.projectbarbel.histo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelTestHelper;

public class DefaultDocumentTest {

	/**
	 * Same instances should return constant hash values.
	 */
	@Test
	public void testHashCode_sameInstance() {
		final DefaultDocument obj = BarbelTestHelper.random(DefaultDocument.class);
		assertEquals(obj.hashCode(), obj.hashCode());
	}

	/**
	 * Different instances should return different hash values.
	 */
	@Test
	public void testHashCode_differentInstances() {
		final DefaultDocument obj1 = BarbelTestHelper.random(DefaultDocument.class);
		final DefaultDocument obj2 = BarbelTestHelper.random(DefaultDocument.class);
		assertNotEquals(obj1.hashCode(), obj2.hashCode());
	}

	/**
	 * Same instances must be equal.
	 */
	@Test
	public void testEqualsObject_sameInstance() {
		final DefaultDocument obj = BarbelTestHelper.random(DefaultDocument.class);
		assertTrue(obj.equals(obj));
	}

	/**
	 * Different instances with different values must be non-equal.
	 */
	@Test
	public void testEqualsObject_differentInstance_differentValues() {
		final DefaultDocument obj1 = BarbelTestHelper.random(DefaultDocument.class);
		final DefaultDocument obj2 = BarbelTestHelper.random(DefaultDocument.class);
		assertFalse(obj1.equals(obj2));
	}
	
	/**
	 * Different Instances with same values should be equal. Copy Constructor.
	 */
	@Test
	public void testEqualsObject_differentInstance_sameValues() {
		final DefaultDocument obj1 = BarbelTestHelper.random(DefaultDocument.class);
		final DefaultDocument obj2 = new DefaultDocument(obj1);
		assertTrue(obj1.equals(obj2));
	}
	
	/**
	 * Different Instances with same values should be equal. Copy Constructor.
	 */
	@Test
	public void testEqualsObject_differentInstance_differentValues_CopyConstructor() {
		final DefaultDocument obj1 = BarbelTestHelper.random(DefaultDocument.class);
		final DefaultDocument obj2 = new DefaultDocument(BarbelTestHelper.random(DefaultDocument.class));
		assertFalse(obj1.equals(obj2));
	}

}
