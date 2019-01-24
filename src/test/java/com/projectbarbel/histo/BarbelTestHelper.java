package com.projectbarbel.histo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultValueObject;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.FieldDefinition;
import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelTestHelper {
    
    public static <T> T random(Class<T> clazz, String... excludedFields) {
        return EnhancedRandomBuilder.aNewEnhancedRandomBuilder().randomize(new FieldDefinition<BitemporalStamp, Serializable>("versionId", Serializable.class, BitemporalStamp.class), new Supplier<String>() {

            @Override
            public String get() {
                return UUID.randomUUID().toString();
            }
        }).build().nextObject(clazz, excludedFields);
    }
    
    public static List<BitemporalStamp> generateListOfBitemporals(String docId, List<LocalDate> effectiveDates) {
        List<BitemporalStamp> journal = new ArrayList<BitemporalStamp>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add(createPeriod(docId, effectiveDates, i));
        }
        return journal;
    }

    private static BitemporalStamp createPeriod(String docId, List<LocalDate> effectiveDates, int listPointer) {
        return BitemporalStamp.create("SYSTEM_PROCESS", docId,
                effectiveDates.size() - 1 == listPointer ? EffectivePeriod.create().from(effectiveDates.get(listPointer)).toInfinite()
                        : EffectivePeriod.create().from(effectiveDates.get(listPointer)).until(effectiveDates.get(listPointer + 1)),
                RecordPeriod.create("SYSTEM_USER"));
    }

    public static List<DefaultValueObject> generateJournalOfDefaultValueObjects(String docId,
            List<LocalDate> effectiveDates) {
        List<DefaultValueObject> journal = new ArrayList<DefaultValueObject>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add(DefaultValueObject.builder()
                    .withBitemporalStamp(createPeriod(docId, effectiveDates, i))
                    .withData(EnhancedRandom.random(String.class)).withVersionId(UUID.randomUUID().toString()).build());
        }
        return journal;
    }

    public static DefaultValueObject createCopyOfDefaultValueObjectWithEffectiveDateAndData(DefaultValueObject template,
            LocalDate effectiveDate, String data) {
        return null;
    }
}
