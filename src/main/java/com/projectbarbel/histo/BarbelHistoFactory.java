package com.projectbarbel.histo;

import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

@FunctionalInterface
public interface BarbelHistoFactory<T> {
    enum FactoryType {

        DAO((options) -> supplierInstanceByClassName("com.projectbarbel.histo.dao.classname", options)),
        SERVICE((options) -> supplierInstanceByClassName("com.projectbarbel.histo.service.classname", options));

        private final BarbelHistoFactory<?> constructor;

        FactoryType(BarbelHistoFactory<?> constructor) {
            this.constructor = constructor;
        }

        private BarbelHistoFactory<?> getConstructor() {
            return this.constructor;
        }
    }

    T create(BarbelHistoOptions options);

    @SuppressWarnings("unchecked")
    static <I> BarbelHistoFactory<I> createFactory(FactoryType type) {
        Validate.notNull(type);
        return (BarbelHistoFactory<I>) type.getConstructor();
    }

    static BarbelHistoFactory<?> createFactory(String type) {
        Validate.notNull(type);
        return FactoryType.valueOf(type.trim().toUpperCase()).getConstructor();
    }

    @SuppressWarnings("unchecked")
    static <O> O supplierInstanceByClassName(String propertyName, BarbelHistoOptions options) {
        Validate.noNullElements(Arrays.asList(propertyName, options));
        try {
            return (O) ((Supplier<O>)Class.forName(options.getOption(propertyName).orElse("")).newInstance()).get();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class name in property " + propertyName + " cannot be found.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("The class name in property " + propertyName + " must be of type java.util.function.Supplier!", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("The class name in property " + propertyName + " could not be instintiated. Check that it has a public default constructor without any arguments.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("The class name in property " + propertyName + " could not be instintiated. Check access rights.", e);
        }
    }

}