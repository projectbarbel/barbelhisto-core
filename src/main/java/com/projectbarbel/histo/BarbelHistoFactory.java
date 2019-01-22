package com.projectbarbel.histo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

public final class BarbelHistoFactory<T> {

    private static Map<String, Supplier<?>> suppliers = new HashMap<String, Supplier<?>>();

    public enum FactoryType {

        DAO((options) -> supplierBySupplierClassName(options.getDaoSupplierClassName())),
        SERVICE((options) -> supplierBySupplierClassName(options.getServiceSupplierClassName()));

        private final Function<BarbelHistoOptions, Supplier<?>> composer;

        FactoryType(Function<BarbelHistoOptions, Supplier<?>> composer) {
            this.composer = composer;
        }

        private Supplier<?> getSupplier(BarbelHistoOptions options) {
            return this.composer.apply(options);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> createFactory(FactoryType type, BarbelHistoOptions options) {
        Validate.noNullElements(Arrays.asList(type, options));
        options.validate();
        return (Supplier<T>) type.getSupplier(options);
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> createFactory(String type, BarbelHistoOptions options) {
        Validate.noNullElements(Arrays.asList(type, options));
        options.validate();
        return (Supplier<T>) FactoryType.valueOf(type.trim().toUpperCase()).getSupplier(options);
    }

    @SuppressWarnings("unchecked")
    public static <O> Supplier<O> createFactory(FactoryType type) {
        Validate.notNull(type);
        return (Supplier<O>) type.getSupplier(BarbelHistoOptions.DEFAULT_CONFIG);
    }

    public static Supplier<?> createFactory(String type) {
        Validate.notNull(type);
        return FactoryType.valueOf(type.trim().toUpperCase()).getSupplier(BarbelHistoOptions.DEFAULT_CONFIG);
    }

    @SuppressWarnings("unchecked")
    public static <O extends Supplier<O>> O supplierBySupplierClassName(String supplierClassName) {
        Validate.noNullElements(Arrays.asList(supplierClassName));
            return (O) suppliers.computeIfAbsent(supplierClassName, BarbelHistoFactory::instantiate);
    }

    @SuppressWarnings("unchecked")
    private static <O extends Supplier<O>> O instantiate(String classname) {
        try {
            return (O)Class.forName(classname).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class " + classname + " cannot be found.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException(
                    "The class " + classname + " must be of type java.util.function.Supplier!", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("The class " + classname
                    + " could not be instintiated. Check that it has a public default constructor without any arguments.",
                    e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "The class " + classname + " could not be instintiated. Check access rights.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProduct(FactoryType dao) {
        return (T) createFactory(dao).get();
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProduct(FactoryType dao, BarbelHistoOptions options) {
        return (T) createFactory(dao, options).get();
    }

}