package com.projectbarbel.histo.persistence.service;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Supplier;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.DefaultValueObject;

public interface DocumentService<T extends Bitemporal<O>, O> {

    public class DocumentServiceProxy implements Supplier<DocumentService<DefaultValueObject, String>> {

        @Override
        public DocumentService<DefaultValueObject, String> get() {
            return proxy();
        }

    }

    @SuppressWarnings("unchecked")
    public static DocumentService<DefaultValueObject, String> proxy() {
        return (DocumentService<DefaultValueObject, String>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[] { DocumentService.class }, (proxy, method, args) -> Optional.of("not implemented"));
    }

    public Optional<O> save(T valueobject);

}
