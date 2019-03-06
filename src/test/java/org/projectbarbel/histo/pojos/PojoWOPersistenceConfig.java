package org.projectbarbel.histo.pojos;

import org.projectbarbel.histo.DocumentId;

public class PojoWOPersistenceConfig {
    @DocumentId
    private String docId;

    public PojoWOPersistenceConfig(String docId) {
        this.docId = docId;
    }
}
