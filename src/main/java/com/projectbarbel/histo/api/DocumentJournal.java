package com.projectbarbel.histo.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.functions.journal.ReaderFunctionGetEffectiveAfter;
import com.projectbarbel.histo.functions.journal.ReaderFunctionGetEffectiveByDate;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.Systemclock;

public class DocumentJournal<T extends Bitemporal<?>> {

    private final List<T> journal = new ArrayList<T>();
    private static final Logger logger = Logger.getLogger(DocumentJournal.class.getName());
    private BiFunction<DocumentJournal<T>, LocalDate, Optional<T>> effectiveReaderFunction = new ReaderFunctionGetEffectiveByDate<T>();
    private BiFunction<DocumentJournal<T>, LocalDate, List<T>> effectiveAfterFunction = new ReaderFunctionGetEffectiveAfter<T>();
    private Systemclock clock = new Systemclock();

    private DocumentJournal(List<T> documentList) {
        super();
        documentList.stream().forEach(journal::add);
    }

    public int size() {
        return journal.size();
    }

    public List<T> list() {
        return journal.stream().collect(Collectors.toCollection(ArrayList::new));
    }

    public void setClock(Systemclock clock) {
        this.clock = clock;
    }

    public static <T extends Bitemporal<?>> DocumentJournal<T> create(List<T> listOfBitemporalDocuments) {
        Validate.notNull(listOfBitemporalDocuments, "new document list must not be null when creating new journal");
        Validate.isTrue(listOfBitemporalDocuments.size() > 0, "list of documents must not be empty");
        DocumentJournal<T> newjournal = (DocumentJournal<T>)new DocumentJournal<T>(listOfBitemporalDocuments);
        return newjournal;
    }

    @SuppressWarnings("unchecked")
    public static <T extends DocumentJournal<O>, O extends Bitemporal<?>> T create(Bitemporal<?> newDocument) {
        Validate.notNull(newDocument, "new document must not be null when creating new journal");
        return (T) new DocumentJournal<Bitemporal<?>>(Collections.singletonList(newDocument));
    }

    public DocumentJournal<T> sortAscendingByEffectiveDate() {
        Collections.sort(journal, Comparator.comparing(Bitemporal::getEffectiveFrom));
        return this;
    }

    public void prettyPrint() {
        logger.log(Level.INFO, this.toString());
    }

    public JournalReader<T> read() {
        return new JournalReader<T>(this, clock );
    }
    
    public JournalUpdater<T> update() {
        return new JournalUpdater<T>(this);
    }

    public static class JournalUpdater<T extends Bitemporal<?>> {
        private DocumentJournal<T> journal;
        private JournalUpdater(DocumentJournal<T> journal) {
            this.journal = journal;
        }
        
        public DocumentJournal<T> processVersionUpdate(VersionUpdate<T> update) {
            return update.updateStrategy.apply(journal, update);
        }
    }
    
    public static class JournalReader<T extends Bitemporal<?>> {
        private DocumentJournal<T> journal;
        private Systemclock clock;

        private JournalReader(DocumentJournal<T> journal, Systemclock clock) {
            this.journal = journal;
            this.clock = clock;
        }

        public EffectiveReader<T> effectiveTime() {
            return new EffectiveReader<T>(journal, clock);
        }

        public RecordtimeReader<T> recordTime() {
            return new RecordtimeReader<T>(journal);
        }

        public List<T> activeVersions() {
            return journal.list().stream().filter((d)->d.isActive()).collect(Collectors.toList());
        }
        
        public List<T> inactiveVersions() {
            return journal.list().stream().filter((d)->!d.isActive()).collect(Collectors.toList());
        }
    }

    public static class EffectiveReader<T extends Bitemporal<?>> {

        private DocumentJournal<T> journal;
        private Systemclock clock;

        public EffectiveReader(DocumentJournal<T> journal, Systemclock clock) {
            this.journal = journal;
            this.clock = clock;
        }

        public List<T> activeVersions() {
            return journal.list().stream().filter((d)->d.isActive()).collect(Collectors.toList());
        }
        public Optional<T> effectiveNow() {
            return journal.effectiveReaderFunction.apply(journal, clock.now().toLocalDate());
        }

        public Optional<T> effectiveAt(LocalDate day) {
            return journal.effectiveReaderFunction.apply(journal, day);
        }

        public List<T> effectiveAfter(LocalDate day) {
            return journal.effectiveAfterFunction.apply(journal, day);
        }

    }

    public static class RecordtimeReader<T extends Bitemporal<?>> {

        @SuppressWarnings("unused")
        private DocumentJournal<T> journal;

        public RecordtimeReader(DocumentJournal<T> journal) {
            this.journal = journal;
        }

    }

    public void add(VersionUpdate<T> update) {
        Validate.notNull(update, "update passed must not be null");
        Validate.validState(update.done(), "this update has not been executed - execute before adding the update to journal");
        Validate.validState(journal.contains(update.result().oldVersion()), "the old version of that update passed is unknown in this journal - please only add valid update whose origin are objects from this journal");
        Validate.validState(!journal.contains(update.result().newPrecedingVersion()), "the new generated preceeding version by this update was already added to the journal");
        Validate.validState(!journal.contains(update.result().newSubsequentVersion()), "the new generated subsequent version by this update was already added to the journal");
        if (journal.contains(update.result().oldVersion())) {
            journal.add(update.result().newPrecedingVersion());
            journal.add(update.result().newSubsequentVersion());
        }
    }

}
