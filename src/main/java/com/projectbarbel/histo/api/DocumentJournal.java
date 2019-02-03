package com.projectbarbel.histo.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.functions.journal.JournalUpdateStrategyEmbedding;
import com.projectbarbel.histo.functions.journal.ReaderFunctionGetEffectiveAfter;
import com.projectbarbel.histo.functions.journal.ReaderFunctionGetEffectiveBetween;
import com.projectbarbel.histo.functions.journal.ReaderFunctionGetEffectiveByDate;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.Systemclock;

public class DocumentJournal<T extends Bitemporal<?>> {

    private final List<T> journal = new ArrayList<T>();
    private BiFunction<DocumentJournal<T>, LocalDate, Optional<T>> effectiveReaderFunction = new ReaderFunctionGetEffectiveByDate<T>();
    private BiFunction<DocumentJournal<T>, LocalDate, List<T>> effectiveAfterFunction = new ReaderFunctionGetEffectiveAfter<T>();
    private BiFunction<DocumentJournal<T>, EffectivePeriod, List<T>> effectiveBetweenFunction = new ReaderFunctionGetEffectiveBetween<T>();
    private BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> updateFunction = new JournalUpdateStrategyEmbedding<T>();

    public void update(VersionUpdateResult<T> update) {
        Validate.notNull(update, "update passed must not be null");
        Validate.validState(journal.contains(update.oldVersion()),
                "the old version of that update passed is unknown in this journal - please only add valid update whose origin are objects from this journal");
        Validate.validState(!journal.contains(update.newPrecedingVersion()),
                "the new generated preceeding version by this update was already added to the journal");
        Validate.validState(!journal.contains(update.newSubsequentVersion()),
                "the new generated subsequent version by this update was already added to the journal");
        if (journal.contains(update.oldVersion())) {
            journal.addAll(updateFunction.apply(this, update));
        }
    }

    @Override
    public String toString() {
        return "DocumentJournal [journal=" + journal + "]";
    }

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

    public static <T extends Bitemporal<?>> DocumentJournal<T> create(List<T> listOfBitemporalDocuments) {
        Validate.notNull(listOfBitemporalDocuments, "new document list must not be null when creating new journal");
        Validate.isTrue(listOfBitemporalDocuments.size() > 0, "list of documents must not be empty");
        Validate.isTrue(listOfBitemporalDocuments.stream().map(Bitemporal::getDocumentId).collect(Collectors.toSet())
                .size() == 1, "the list passed must only contain bitemporal objects for the same document id");
        DocumentJournal<T> newjournal = (DocumentJournal<T>) new DocumentJournal<T>(listOfBitemporalDocuments);
        return newjournal;
    }

    @SuppressWarnings("unchecked")
    public static <T extends DocumentJournal<O>, O extends Bitemporal<?>> T create(Bitemporal<?> newDocument) {
        Validate.notNull(newDocument, "new document must not be null when creating new journal");
        Optional.ofNullable(newDocument).filter((d) -> d.getBitemporalStamp() == null)
                .ifPresent((d) -> d.setBitemporalStamp(BitemporalStamp::initial));
        Validate.validState(newDocument.getBitemporalStamp() != null, "failed to initialize stamp");
        return (T) new DocumentJournal<Bitemporal<?>>(Collections.singletonList(newDocument));
    }

    public DocumentJournal<T> sortAscendingByEffectiveDate() {
        Collections.sort(journal, Comparator.comparing(Bitemporal::getEffectiveFrom));
        return this;
    }

    // @formatter:off
    public String prettyPrint() {
        return "\n" + "Document-ID: " + (journal.size() > 0 ? journal.get(0).getDocumentId() : "<empty jounral>")
                + "\n\n"
                + String.format("|%-40s|%-15s|%-16s|%-8s|%-21s|%-23s|%-21s|%-23s|", "Version-ID", "Effective-From",
                        "Effective-Until", "State", "Created-By", "Created-At", "Inactivated-By", "Inactivated-At")
                + "\n|" + StringUtils.leftPad("|", 41, "-") + StringUtils.leftPad("|", 16, "-")
                + StringUtils.leftPad("|", 17, "-") + StringUtils.leftPad("|", 9, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 24, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 24, "-") + "\n"
                + journal.stream().map(Bitemporal::prettyPrint).collect(Collectors.joining("\n"));
    }
    // @formatter:on

    public JournalReader<T> read() {
        return new JournalReader<T>(this, BarbelHistoContext.instance().clock());
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
            return journal.list().stream().filter((d) -> d.isActive()).collect(Collectors.toList());
        }

        public List<T> inactiveVersions() {
            return journal.list().stream().filter((d) -> !d.isActive()).collect(Collectors.toList());
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
            return journal.list().stream().filter((d) -> d.isActive()).collect(Collectors.toList());
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

        public List<T> effectiveBetween(EffectivePeriod effectiveTime) {
            return journal.effectiveBetweenFunction.apply(journal, effectiveTime);
        }

    }

    public static class RecordtimeReader<T extends Bitemporal<?>> {

        @SuppressWarnings("unused")
        private DocumentJournal<T> journal;

        public RecordtimeReader(DocumentJournal<T> journal) {
            this.journal = journal;
        }

    }

}
