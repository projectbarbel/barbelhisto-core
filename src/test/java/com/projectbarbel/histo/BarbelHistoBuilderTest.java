package com.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.javers.common.collections.Arrays;
import org.junit.jupiter.api.Test;

public class BarbelHistoBuilderTest {

    @Test
    public void testBarbel_testAssignment() throws Exception {
        BarbelHisto barbel = BarbelHistoBuilder.barbel().build();
        assertNotNull(barbel);
    }

    @Test
    public void testBarbel_testBuilder() throws Exception {
        long countOfWith = Arrays.asList(BarbelHistoBuilder.class.getMethods()).stream()
                .filter(m -> ((Method) m).getName().startsWith("with")).count();
        long countOfBarbelReturner = Arrays.asList(BarbelHistoBuilder.class.getMethods()).stream()
                .filter(m -> ((Method) m).getName().startsWith("with"))
                .filter(m -> ((Method) m).getReturnType().equals(BarbelHistoBuilder.class)).count();
        assertEquals(countOfWith, countOfBarbelReturner);
    }

    @Test
    public void testBarbel_testAllFieldsHaveWithAccessor() throws Exception {
        long countOfFields = Arrays.asList(BarbelHistoBuilder.class.getDeclaredFields()).stream()
                .filter(f -> !Modifier.isStatic(((Field) f).getModifiers())).count();
        long countOfWithAccessors = Arrays.asList(BarbelHistoBuilder.class.getMethods()).stream()
                .filter(m -> ((Method) m).getName().startsWith("with")).count();
        assertEquals(countOfFields, countOfWithAccessors);
    }

    @Test
    public void testBarbel_testAccessors() throws Exception {
        BarbelHistoBuilder builder = BarbelHistoBuilder.barbel();
        for (Field f : BarbelHistoBuilder.class.getDeclaredFields()) {
            String withMethodName = "with" + StringUtils.capitalize(f.getName());
            String getMethodName = "get" + StringUtils.capitalize(f.getName());
            if (!Modifier.isStatic(f.getModifiers())) {
                Object object = MethodUtils.invokeMethod(builder, withMethodName,
                        MethodUtils.invokeMethod(builder, getMethodName, null));
                assertNotNull(object);
            }
        }
    }

    @Test
    public void testBarbel_testHasDefaultValues() throws Exception {
        BarbelHistoBuilder builder = BarbelHistoBuilder.barbel();
        for (Field f : BarbelHistoBuilder.class.getDeclaredFields()) {
            String getMethodName = "get" + StringUtils.capitalize(f.getName());
            if (!Modifier.isStatic(f.getModifiers())) {
                Object object = MethodUtils.invokeMethod(builder, getMethodName, null);
                assertNotNull(object);
            }
        }
    }

    @Test
    public void testBarbel_testNullSettingThrowsException() throws Exception {
        BarbelHistoBuilder builder = BarbelHistoBuilder.barbel();
        for (Method m : BarbelHistoBuilder.class.getMethods()) {
            if (m.getName().startsWith("with")) {
                InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                        () -> m.invoke(builder, new Object[] { null }));
                assertTrue(exception.getCause() instanceof IllegalArgumentException);
            }
        }
    }

}
