package org.projectbarbel.histo.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.projectbarbel.histo.model.BitemporalVersion;

import com.google.gson.Gson;

public class DefaultLazyLoadingListener extends AbstractLazyLoadingListener<List<BitemporalVersion>, BitemporalVersion>{

    public static List<BitemporalVersion> shadow = new ArrayList<>();
    
    protected DefaultLazyLoadingListener(Class<?> managedType, Gson gson, boolean singletonContext) {
        super(managedType, gson, singletonContext);
    }

    @Override
    public Iterable<BitemporalVersion> queryAll() {
        return shadow; 
    }

    @Override
    public Iterable<BitemporalVersion> queryJournal(Object id) {
        return shadow.stream().filter(d->d.getBitemporalStamp().getDocumentId().equals(id)).collect(Collectors.toList());
    }

    @Override
    public List<BitemporalVersion> getExternalDataResource() {
        return shadow;
    }

    @Override
    public String fromStoredDocumentToPersistenceObjectJson(BitemporalVersion document) {
        return gson.toJson(document.getObject());
    }
    
}
