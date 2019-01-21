package com.projectbarbel.histo;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

public final class BarbelHistoFactory<T> {
    
    public enum FactoryType {

        DAO((options) -> supplierBySupplierClassName(options.getDaoClassName())),
        SERVICE((options) -> supplierBySupplierClassName(options.getServiceClassName()));

        private final Function<BarbelHistoOptions, Supplier<?>> composer;

        FactoryType(Function<BarbelHistoOptions, Supplier<?>> composer) {
            this.composer = composer;
        }

        private Supplier<?> getSupplier(BarbelHistoOptions options) {
            return this.composer.apply(options);
        }
    }

    public static Supplier<?> createFactory(FactoryType type, BarbelHistoOptions options) {
        Validate.noNullElements(Arrays.asList(type, options));
        options.validate();
        return type.getSupplier(options);
    }

    public static Supplier<?> createFactory(String type, BarbelHistoOptions options) {
        Validate.noNullElements(Arrays.asList(type, options));
        options.validate();
        return FactoryType.valueOf(type.trim().toUpperCase()).getSupplier(options);
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
        try {
            return (O)Class.forName(supplierClassName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class " + supplierClassName + " cannot be found.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("The class " + supplierClassName + " must be of type java.util.function.Supplier!", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("The class " + supplierClassName + " could not be instintiated. Check that it has a public default constructor without any arguments.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("The class " + supplierClassName  + " could not be instintiated. Check access rights.", e);
        }
    }

}