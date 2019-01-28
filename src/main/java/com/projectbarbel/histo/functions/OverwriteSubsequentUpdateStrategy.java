package com.projectbarbel.histo.functions;

import java.util.function.Function;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public class OverwriteSubsequentUpdateStrategy implements Function<DocumentJournal<Bitemporal<?>, ?>, DocumentJournal<Bitemporal<?>, ?>> {

    @Override
    public DocumentJournal<Bitemporal<?>, ?> apply(DocumentJournal<Bitemporal<?>, ?> t) {
        return t;
    }

}
