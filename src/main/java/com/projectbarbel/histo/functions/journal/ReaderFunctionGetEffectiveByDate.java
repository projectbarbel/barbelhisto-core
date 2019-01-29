package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public class ReaderFunctionGetEffectiveByDate<T extends Bitemporal<?>>
        implements BiFunction<DocumentJournal<T>, LocalDate, Optional<T>> {

    
    @Override
    public Optional<T> apply(DocumentJournal<T> journal, LocalDate day) {
        Validate.notNull(journal, "null value for journal not welcome here");
        @SuppressWarnings("unchecked")
        List<T> documents = (List<T>) journal.list();
        JournalPredicates predicates = new JournalPredicates(day);
        List<T> currentVersions = documents.stream().filter(predicates::isActive)
                .filter(predicates::effectiveOn).collect(Collectors.toList());
        if (currentVersions.size() == 0)
            return Optional.empty();
        else if (currentVersions.size() == 1)
            return Optional.of(currentVersions.get(0));
        else
            throw new IllegalStateException("too many current active versions - insonsistent journal");
    }
}
