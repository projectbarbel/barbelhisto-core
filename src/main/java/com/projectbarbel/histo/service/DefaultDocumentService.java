package com.projectbarbel.histo.service;

import java.util.List;
import java.util.Optional;

import com.projectbarbel.histo.dao.DocumentDao;
import com.projectbarbel.histo.model.DefaultValueObject;

public class DefaultDocumentService implements DocumentService<DefaultValueObject, String> {

    private final DocumentDao<DefaultValueObject, String> dao;
    
    public DefaultDocumentService(DocumentDao<DefaultValueObject, String> dao) {
        super();
        this.dao = dao;
    }

    @Override
    public Optional<String> save(DefaultValueObject valueobject) {
        List<DefaultValueObject> journal = dao.readJournal();
        return null;
    }

}
