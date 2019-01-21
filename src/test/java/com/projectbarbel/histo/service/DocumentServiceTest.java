package com.projectbarbel.histo.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.projectbarbel.histo.model.DefaultValueObject;

public class DocumentServiceTest {

    @Test
    public void testProxy_create() {
        DocumentService<DefaultValueObject, String> service = DocumentService.proxy();
        assertEquals(service.save(DefaultValueObject.builder().build()).get(), "not implemented");
    }

}
