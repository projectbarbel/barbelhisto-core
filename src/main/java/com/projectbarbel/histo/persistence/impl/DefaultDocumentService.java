package com.projectbarbel.histo.persistence.impl;

import java.util.Optional;

import com.projectbarbel.histo.model.DefaultDocument;

public class DefaultDocumentService implements DocumentService<DefaultDocument, String> {

    private final DocumentDao<DefaultDocument, String> dao;
    
    public DefaultDocumentService(DefaultDocumentDao dao) {
        super();
        this.dao = dao;
    }

    @Override
    public Optional<String> save(DefaultDocument valueobject) {
        return dao.createDocument(valueobject);
    }

}
