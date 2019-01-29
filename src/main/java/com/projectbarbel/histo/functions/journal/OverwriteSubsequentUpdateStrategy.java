package com.projectbarbel.histo.functions.journal;

import java.util.function.Function;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public class OverwriteSubsequentUpdateStrategy implements Function<DocumentJournal<? extends Bitemporal<?>>,DocumentJournal<? extends Bitemporal<?>>> {

    @Override
    public DocumentJournal<? extends Bitemporal<?>> apply(DocumentJournal<? extends Bitemporal<?>> t) {
        // TODO Auto-generated method stub
        return null;
    }

}
