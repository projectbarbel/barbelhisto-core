package com.projectbarbel.histo.service;

import java.util.Optional;
import java.util.function.Supplier;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.dao.DocumentDao;
import com.projectbarbel.histo.model.DefaultValueObject;

public class DefaultDocumentService implements DocumentService<DefaultValueObject, String> {

    public static class DefaultDocumentServiceSupplier implements Supplier<DefaultDocumentService> {

        @Override
        public DefaultDocumentService get() {
            return new DefaultDocumentService(BarbelHistoFactory.createProduct(FactoryType.DAO, BarbelHistoOptions.DEFAULT_CONFIG));
        }
        
    }
    
    private final DocumentDao<DefaultValueObject, String> dao;
    
    public DefaultDocumentService(DocumentDao<DefaultValueObject, String> dao) {
        super();
        this.dao = dao;
    }

    @Override
    public Optional<String> save(DefaultValueObject valueobject) {
        return dao.createDocument(valueobject);
    }

}
