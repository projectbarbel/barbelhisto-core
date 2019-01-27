package com.projectbarbel.histo.model;

import java.util.function.Function;

public class KeepSubsequentUpdateStrategy implements Function<DocumentJournal<Bitemporal<?>, ?>, DocumentJournal<Bitemporal<?>, ?>> {

    @Override
    public DocumentJournal<Bitemporal<?>, ?> apply(DocumentJournal<Bitemporal<?>, ?> t) {
        return t;
    }

}
