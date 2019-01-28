package com.projectbarbel.histo.functions;

import java.util.function.BiFunction;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.api.VersionUpdate;
import com.projectbarbel.histo.model.Bitemporal;

public class KeepSubsequentUpdateStrategy implements BiFunction<DocumentJournal<? extends Bitemporal<?>>,VersionUpdate, DocumentJournal<? extends Bitemporal<?>>> {

    @Override
    public DocumentJournal<? extends Bitemporal<?>> apply(DocumentJournal<? extends Bitemporal<?>> t, VersionUpdate update) {
        return null;
    }

}
