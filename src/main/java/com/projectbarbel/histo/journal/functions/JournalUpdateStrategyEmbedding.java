package com.projectbarbel.histo.journal.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.UpdateCaseAware;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;

public class JournalUpdateStrategyEmbedding implements BiConsumer<DocumentJournal, Bitemporal>, UpdateCaseAware {

    private final BarbelHistoContext context;
    private List<Bitemporal> newVersions = new ArrayList<>();
    private JournalUpdateCase actualCase;

    public JournalUpdateCase getActualCase() {
        return actualCase;
    }

    public JournalUpdateStrategyEmbedding(BarbelHistoContext context) {
        this.context = context;
    }

    @Override
    public void accept(DocumentJournal journal, final Bitemporal update) {
        Validate.isTrue(journal.getId().equals(update.getBitemporalStamp().getDocumentId()),
                "update and journal must have same document id");
        Validate.isTrue(update.getBitemporalStamp().isActive(), "only active bitemporals are allowed here");
        Optional<Bitemporal> interruptedLeftVersion = journal.read().effectiveTime()
                .effectiveAt(update.getBitemporalStamp().getEffectiveTime().from());
        Optional<Bitemporal> interruptedRightVersion = journal.read().effectiveTime()
                .effectiveAt(update.getBitemporalStamp().getEffectiveTime().until());
        IndexedCollection<Bitemporal> betweenVersions = journal.read().effectiveTime()
                .effectiveBetween(update.getBitemporalStamp().getEffectiveTime());
        actualCase = JournalUpdateCase.validate(interruptedLeftVersion.isPresent(), interruptedRightVersion.isPresent(),
                interruptedLeftVersion.equals(interruptedRightVersion), !betweenVersions.isEmpty());
        newVersions.add(update);
        interruptedLeftVersion.ifPresent(d -> processInterruptedLeftVersion(update, d));
        interruptedRightVersion.ifPresent(d -> processInterruptedRightVersion(update, d));
        interruptedLeftVersion
                .ifPresent(d -> d.setBitemporalStamp(d.getBitemporalStamp().inactivatedCopy(context)));
        interruptedRightVersion
                .ifPresent(d -> d.setBitemporalStamp(d.getBitemporalStamp().inactivatedCopy(context)));
        betweenVersions.stream()
                .forEach(d -> d.setBitemporalStamp(d.getBitemporalStamp().inactivatedCopy(context)));
        journal.accept(newVersions);
    }

    private void processInterruptedLeftVersion(final Bitemporal update, Bitemporal interruptedLeftVersion) {
        Bitemporal newPrecedingVersion = context.getMode().snapshotManagedBitemporal(context, interruptedLeftVersion,
                BitemporalStamp.createActive(context, update.getBitemporalStamp().getDocumentId(),
                        EffectivePeriod.of(interruptedLeftVersion.getBitemporalStamp().getEffectiveTime().from(),
                                update.getBitemporalStamp().getEffectiveTime().from())));
        newVersions.add(newPrecedingVersion);
    }

    private void processInterruptedRightVersion(final Bitemporal update, Bitemporal interruptedRightVersion) {
        Bitemporal newSubsequentVersion = context.getMode().snapshotManagedBitemporal(context, interruptedRightVersion,
                BitemporalStamp.createActive(context, update.getBitemporalStamp().getDocumentId(),
                        EffectivePeriod.of(update.getBitemporalStamp().getEffectiveTime().until(),
                                interruptedRightVersion.getBitemporalStamp().getEffectiveTime().until())));
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

        public static JournalUpdateCase validate(boolean interruptedFrom, boolean interruptedUntil, boolean interruptedEqual,
                boolean betweenVersions) {
            byte pattern = asByte(
                    new boolean[] { interruptedFrom, interruptedUntil, interruptedEqual, betweenVersions });
            JournalUpdateCase validCase = Arrays.asList(JournalUpdateCase.values()).stream().filter(c -> pattern == c.getPattern()).findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "unknown case for journal update: " + Byte.toString(pattern)));
            return validCase;
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
