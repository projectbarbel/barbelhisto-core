package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public class ReaderFunctionGetEffectiveAfter<T extends Bitemporal<?>>
        implements BiFunction<DocumentJournal<T>, LocalDate, List<T>> {

    @Override
    public List<T> apply(DocumentJournal<T> journal, LocalDate day) {
        Validate.notNull(journal, "null value for journal not welcome here");
        JournalPredicates predicates = new JournalPredicates(day);
        List<T> documents = (List<T>) journal.list();
        return documents.stream().filter(predicates::isActive).filter(predicates::effectiveAfter)
                .collect(Collectors.toList());

    }
}
