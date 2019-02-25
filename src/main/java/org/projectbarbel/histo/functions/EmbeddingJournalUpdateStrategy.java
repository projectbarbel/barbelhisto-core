package org.projectbarbel.histo.functions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.UpdateCaseAware;

/**
 * The heart of {@link BarbelHisto} where the journal updates are performed
 * according to the bitemporal data rules.
 * 
 * @author Niklas Schlimm
 *
 */
public class EmbeddingJournalUpdateStrategy implements BiConsumer<DocumentJournal, Bitemporal>, UpdateCaseAware {

    private final BarbelHistoContext context;
    private List<Bitemporal> newVersions = new ArrayList<>();
    private JournalUpdateCase actualCase;

    public JournalUpdateCase getActualCase() {
        return actualCase;
    }

    public EmbeddingJournalUpdateStrategy(BarbelHistoContext context) {
        this.context = context;
    }

    @Override
    public void accept(DocumentJournal journal, final Bitemporal update) {
        Validate.isTrue(journal.getId().equals(update.getBitemporalStamp().getDocumentId()),
                "update and journal must have same document id");
        Validate.isTrue(update.getBitemporalStamp().isActive(), "only active bitemporals are allowed here");
        LocalDate rightBound = update.getBitemporalStamp().getEffectiveTime().until().equals(LocalDate.MAX)
                ? LocalDate.MAX
                : update.getBitemporalStamp().getEffectiveTime().until().minusDays(1);
        Optional<Bitemporal> interruptedLeftVersion = journal.read()
                .effectiveAt(update.getBitemporalStamp().getEffectiveTime().from());
        Optional<Bitemporal> interruptedRightVersion = journal.read().effectiveAt(rightBound);
        List<Bitemporal> betweenVersions = journal.read()
                .effectiveBetween(update.getBitemporalStamp().getEffectiveTime());
        actualCase = JournalUpdateCase.validate(interruptedLeftVersion.isPresent(), interruptedRightVersion.isPresent(),
                interruptedLeftVersion.equals(interruptedRightVersion), !betweenVersions.isEmpty());
        journal.setLastUpdateCase(actualCase);
        newVersions.add(update);
        interruptedLeftVersion.ifPresent(d -> processInterruptedLeftVersion(update, d));
        interruptedRightVersion.ifPresent(d -> processInterruptedRightVersion(update, d));
        interruptedLeftVersion.ifPresent(inactivate(journal));
        interruptedRightVersion.ifPresent(inactivate(journal));
        betweenVersions.stream().forEach(inactivate(journal));
        journal.insert(newVersions);
    }

	private Consumer<? super Bitemporal> inactivate(final DocumentJournal journal) {
		return orginal -> {
			Bitemporal copy = context.getMode().copyManagedBitemporal(context, orginal);
			copy.setBitemporalStamp(copy.getBitemporalStamp().inactivatedCopy(context));
			journal.replace(Collections.singletonList(orginal), Collections.singletonList(copy));
		};
	}

    private void processInterruptedLeftVersion(final Bitemporal update, Bitemporal interruptedLeftVersion) {
        Bitemporal newPrecedingVersion = context.getMode().snapshotManagedBitemporal(context, interruptedLeftVersion,
                BitemporalStamp.createActive(context, update.getBitemporalStamp().getDocumentId(),
                        EffectivePeriod.of(interruptedLeftVersion.getBitemporalStamp().getEffectiveTime().from(),
                                update.getBitemporalStamp().getEffectiveTime().from())));
        if (newPrecedingVersion.getBitemporalStamp().getEffectiveTime().from()
                .isBefore(newPrecedingVersion.getBitemporalStamp().getEffectiveTime().until()))
            newVersions.add(newPrecedingVersion);
    }

    private void processInterruptedRightVersion(final Bitemporal update, Bitemporal interruptedRightVersion) {
        Bitemporal newSubsequentVersion = context.getMode().snapshotManagedBitemporal(context, interruptedRightVersion,
                BitemporalStamp.createActive(context, update.getBitemporalStamp().getDocumentId(),
                        EffectivePeriod.of(update.getBitemporalStamp().getEffectiveTime().until(),
                                interruptedRightVersion.getBitemporalStamp().getEffectiveTime().until())));
        if (newSubsequentVersion.getBitemporalStamp().getEffectiveTime().from()
                .isBefore(newSubsequentVersion.getBitemporalStamp().getEffectiveTime().until()))
            newVersions.add(newSubsequentVersion);
    }

    public enum JournalUpdateCase {

        //// @formatter:off

        STRAIGHTINSERT(asByte(new boolean[] {false, false, true, false})),
        // A: <no record of this id>     
        // U: |-------|
        
        PREOVERLAPPING(asByte(new boolean[] {false, true, false, false})),   
        // A:      |---------|
        // U: |-------|
                                   
        POSTOVERLAPPING(asByte(new boolean[] {true, false, false, false})), 
        // A: |-------|
        // U:      |---------|

        EMBEDDEDINTERVAL(asByte(new boolean[] {true, true, true, false})),  
        // A: |--------------|
        // U:     |------|
        
        EMBEDDEDOVERLAP(asByte(new boolean[] {true, true, false, false})),   
        // A: |-------|------|------|
        // U:     |-------|
                                   
        OVERLAY(asByte(new boolean[] {false, false, true, true})),    
        // A:         |------|
        // U:     |--------------|
        
        EMBEDDEDOVERLAY(asByte(new boolean[] {true, true, false, true})),    
        // A: |-------|------|------|
        // U:     |--------------|

        PREOVERLAPPING_OVERLAY(asByte(new boolean[] {false, true, false, true})),    
        // A:     |-------|------|
        // U: |--------------|
        
        POSTOVERLAPPING_OVERLAY(asByte(new boolean[] {true, false, false, true}));    
        // A: |------|------|
        // U:    |--------------|

        // @formatter:on

        private byte pattern;

        private JournalUpdateCase(byte pattern) {
            this.pattern = pattern;
        }

        public static JournalUpdateCase validate(boolean interruptedFrom, boolean interruptedUntil,
                boolean interruptedEqual, boolean betweenVersions) {
            byte pattern = asByte(
                    new boolean[] { interruptedFrom, interruptedUntil, interruptedEqual, betweenVersions });
            return Arrays.asList(JournalUpdateCase.values()).stream()
                    .filter(c -> pattern == c.getPattern()).findFirst().orElseThrow(() -> new IllegalStateException(
                            "unknown case for journal update: " + Byte.toString(pattern)));
        }

        private byte getPattern() {
            return pattern;
        }

    }

    private static byte asByte(boolean[] source) {
        byte result = 0;

        int index = 8 - source.length;

        for (int i = 0; i < source.length; i++) {
            if (source[i])
                result |= (byte) (1 << (7 - index));
            index++;
        }
        return result;
    }

}
