package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;

public class BarbelModeProcessorTest {

    @Test
    public void getDocumentIdFieldName() throws Exception {
        assertEquals("bitemporalStamp.documentId",BarbelMode.POJO.getDocumentIdFieldNameOnPersistedType(DefaultPojo.class));
    }

    @Test
    public void testGetIdValue() throws Exception {
        assertEquals("id",BarbelMode.POJO.getIdValue(new DefaultPojo("id", "data")).get());
    }

    @Test
    public void getPersistenceObjectType_POJO() throws Exception {
        assertEquals(BitemporalVersion.class,BarbelMode.POJO.getPersistenceObjectType(DefaultPojo.class));
    }

    @Test
    public void getPersistenceObjectType_BITEMPORAL() throws Exception {
        assertEquals(DefaultDocument.class,BarbelMode.BITEMPORAL.getPersistenceObjectType(DefaultDocument.class));
    }
    
    @Test
    public void getPersistenceObjectType() throws Exception {
        assertEquals("bitemporalStamp",BarbelMode.BITEMPORAL.getStampFieldName(DefaultDocument.class, BitemporalStamp.class));
    }
    
}
