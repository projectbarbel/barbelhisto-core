package com.projectbarbel.histo.persistence.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.model.DefaultDocument;

public class DefaultDocumentDao implements DocumentDao<DefaultDocument, String> {

    private Map<String, DefaultDocument> store = new ConcurrentHashMap<>();

    @Override
    public Optional<String> createDocument(DefaultDocument document) {
        Validate.notNull(document);
        Optional.ofNullable(store.putIfAbsent(document.getVersionId(), document)).ifPresent((d)->throwException("duplicate key: "+d.getVersionId()));
        return Optional.of(document.getVersionId());
    }

    private void throwException(String message) {
        throw new RuntimeException(message);
    }

    @Override
    public Optional<String> updateDocument(String objectId, DefaultDocument document) {
        Validate.notNull(document);
        Optional.ofNullable(store.replace(objectId,document)).orElseThrow(()->new RuntimeException("no record to update"));
        return Optional.of(document.getVersionId());
    }

    @Override
    public long deleteDocument(String objectId) {
        return Optional.ofNullable(store.remove(objectId)).isPresent()?1:0;
    }

    @Override
    public Optional<DefaultDocument> readDocument(String objectId) {
        return Optional.ofNullable(store.get(objectId));
    }

    @Override
    public void reset() {
        store = new ConcurrentHashMap<String, DefaultDocument>();
    }

    @Override
    public List<DefaultDocument> readJournal() {
        return null;
    }

}
