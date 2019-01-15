package com.projectbarbel.histo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Test;

import io.github.benas.randombeans.api.EnhancedRandom;

public class DefaultValueObjectTest {

	/**
	 * Test if hashCode returns the correct values. If {@link DefaultValueObject} is changed internally
	 * and the hashCode Method is not adopted correspondingly, then this test will fail.
	 */
	@Test
	public void testHashCode_isCorrect() {
		final DefaultValueObject obj = EnhancedRandom.random(DefaultValueObject.class);
		assertEquals(obj.hashCode(), Objects.hash(getFieldsByReflection(obj)));
	}

	private Object[] getFieldsByReflection(final DefaultValueObject obj) {
		List<Object> declaredfields=Arrays.asList(obj.getClass().getSuperclass().getDeclaredFields()).stream().map(f->{
			try {
				f.setAccessible(true);
				return f.get(obj);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
		declaredfields.addAll(Arrays.asList(obj.getClass().getDeclaredFields()).stream().map(f->{
			try {
				f.setAccessible(true);
				return f.get(obj);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList()));
		return declaredfields.toArray();
	}

	/**
	 * Same instances should return constant hash values.
	 */
	@Test
	public void testHashCode_sameInstance() {
		final DefaultValueObject obj = EnhancedRandom.random(DefaultValueObject.class);
		assertEquals(obj.hashCode(), obj.hashCode());
	}

	/**
	 * Different instances should return different hash values.
	 */
	@Test
	public void testHashCode_differentInstances() {
		final DefaultValueObject obj1 = EnhancedRandom.random(DefaultValueObject.class);
		final DefaultValueObject obj2 = EnhancedRandom.random(DefaultValueObject.class);
		assertNotEquals(obj1.hashCode(), obj2.hashCode());
	}

	/**
	 * Same instances must be equal.
	 */
	@Test
	public void testEqualsObject_sameInstance() {
		final DefaultValueObject obj = EnhancedRandom.random(DefaultValueObject.class);
		assertTrue(obj.equals(obj));
	}

	/**
	 * Different instances with different values must be non-equal.
	 */
	@Test
	public void testEqualsObject_differentInstance_differentValues() {
		final DefaultValueObject obj1 = EnhancedRandom.random(DefaultValueObject.class);
		final DefaultValueObject obj2 = EnhancedRandom.random(DefaultValueObject.class);
		assertFalse(obj1.equals(obj2));
	}
	
	/**
	 * Different Instances with same values should be equal. Copy Constructor.
	 */
	@Test
	public void testEqualsObject_differentInstance_sameValues() {
		final DefaultValueObject obj1 = EnhancedRandom.random(DefaultValueObject.class);
		final DefaultValueObject obj2 = new DefaultValueObject(obj1);
		assertTrue(obj1.equals(obj2));
	}
	
	/**
	 * Different Instances with same values should be equal. Copy Constructor.
	 */
	@Test
	public void testEqualsObject_differentInstance_differentValues_CopyConstructor() {
		final DefaultValueObject obj1 = EnhancedRandom.random(DefaultValueObject.class);
		final DefaultValueObject obj2 = new DefaultValueObject(EnhancedRandom.random(DefaultValueObject.class));
		assertFalse(obj1.equals(obj2));
	}

	@Test()
	public void testArgumentValidationInConstructor_Valid() {
		BitemporalStamp stamp = new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class));
		assertNotNull(stamp);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_documentId () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "documentId"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_effectiveFrom () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "effectiveFrom"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_createdAt () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "createdAt"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_createdBy () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "createdBy"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_inactivatedAt () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "inactivatedAt"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_status () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "status"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_inactivatedBy () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "inactivatedBy"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentValidationInConstructor_activity () {
		new BitemporalStamp(EnhancedRandom.random(BitemporalStamp.class, "activity"));
	}
	
}
