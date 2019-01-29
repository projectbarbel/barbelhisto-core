package com.projectbarbel.histo.api;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.projectbarbel.histo.model.Bitemporal;

public class BitemporalFactory {

    @SuppressWarnings("unchecked")
    public static <T> Bitemporal<T> create(Class<T> bitemporalType, Object... constructorargs) {
        try {
            return (Bitemporal<T>)ConstructorUtils.invokeConstructor(bitemporalType, constructorargs);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | InstantiationException e) {
            
        }
        return null;
    }

}
