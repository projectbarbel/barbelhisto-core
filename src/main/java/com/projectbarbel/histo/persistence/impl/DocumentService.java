package com.projectbarbel.histo.persistence.impl;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Supplier;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.DefaultDocument;

public interface DocumentService<T extends Bitemporal<O>, O> {

    public class DocumentServiceProxy implements Supplier<DocumentService<DefaultDocument, String>> {

        @Override
        public DocumentService<DefaultDocument, String> get() {
            return proxy();
        }

    }

    @SuppressWarnings("unchecked")
    public static DocumentService<DefaultDocument, String> proxy() {
        return (DocumentService<DefaultDocument, String>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[] { DocumentService.class }, (proxy, method, args) -> Optional.of("not implemented"));
    }

    public Optional<O> save(T valueobject);

}
