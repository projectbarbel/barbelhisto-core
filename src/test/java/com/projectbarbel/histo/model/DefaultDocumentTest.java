package com.projectbarbel.histo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;

public class DefaultDocumentTest {

	/**
	 * BlockTest if hashCode returns the correct values. If {@link DefaultDocument} is changed internally
	 * and the hashCode Method is not adopted correspondingly, then this test will fail.
	 */
	@Test
	public void testHashCode_isCorrect() {
		final DefaultDocument obj = BarbelTestHelper.random(DefaultDocument.class);
		assertEquals(obj.hashCode(), Objects.hash(getFieldsByReflection(obj)));
	}

	private Object[] getFieldsByReflection(final DefaultDocument obj) {
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
