package com.projectbarbel.histo.persistence.api;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.DefaultDocument;

/**
 * Generic DAO Interface for bi-temporal data storage.
 * 
 * @author Niklas Schlimm
 *
 * @param <T> the application's document type to store with a concrete DAO
 *        implementation
 * @param <O> the object id type that is used by the application as unique
 *        object identifier
 */
public interface DocumentDao<T extends Bitemporal<O>, O> {

    public class ProxySupplier implements Supplier<DocumentDao<DefaultDocument, String>> {

        @Override
        public DocumentDao<DefaultDocument, String> get() {
            return proxy();
        }

    }

    @SuppressWarnings("unchecked")
    public static DocumentDao<DefaultDocument, String> proxy() {
        return (DocumentDao<DefaultDocument, String>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[] { DocumentDao.class }, (proxy, method,
                        args) -> !method.getName().equals("deleteDocument") ? Optional.of("not implemented") : 42L);
    }

    Optional<O> createDocument(T document);

    Optional<O> updateDocument(O objectId, T document);

    long deleteDocument(O objectId);

    Optional<T> readDocument(O objectId);

    void reset();

    List<DefaultDocument> readJournal();

}