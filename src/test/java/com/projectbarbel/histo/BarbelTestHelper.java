package com.projectbarbel.histo;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultIDGenerator;
import com.projectbarbel.histo.model.DefaultValueObject;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.FieldDefinition;
import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelTestHelper {

    @SuppressWarnings("rawtypes")
    public static <T> T random(Class<T> clazz, String... excludedFields) {
        return EnhancedRandomBuilder.aNewEnhancedRandomBuilder().randomize(Serializable.class, new Supplier<String>() {

            @Override
            public String get() {
                return UUID.randomUUID().toString();
            }
        }).randomize(LocalDate.class, new Supplier<LocalDate>() {

            @Override
            public LocalDate get() {
                return BarbelTestHelper.randomLocalDate(2000, 2020);
            }
        }).randomize(EffectivePeriod.class, new Supplier<EffectivePeriod>() {

            @Override
            public EffectivePeriod get() {
                LocalDate effectiveFrom = randomLocalDate(2000, 2020);
                return EffectivePeriod.create().from(effectiveFrom).until(randomLocalDate(effectiveFrom.plusDays(1), 2020));
            }
        }).randomize(RecordPeriod.class, new Supplier<RecordPeriod>() {

            @Override
            public RecordPeriod get() {
                return RecordPeriod.create(EnhancedRandom.random(String.class), randomInstant(2000, 2020));
            }
        }).randomize(new FieldDefinition<BitemporalStamp, Supplier>("idSupplier", Supplier.class, BitemporalStamp.class),new Supplier<DefaultIDGenerator>() {

            @Override
            public DefaultIDGenerator get() {
                return new DefaultIDGenerator();
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
        return BitemporalStamp
                .of("SYSTEM_PROCESS", docId,
                        effectiveDates.size() - 1 == listPointer
                                ? EffectivePeriod.create().from(effectiveDates.get(listPointer)).toInfinite()
                                : EffectivePeriod.create().from(effectiveDates.get(listPointer))
                                        .until(effectiveDates.get(listPointer + 1)),
                        RecordPeriod.create("SYSTEM_USER"));
    }

    public static List<DefaultValueObject> generateJournalOfDefaultValueObjects(String docId,
            List<LocalDate> effectiveDates) {
        List<DefaultValueObject> journal = new ArrayList<DefaultValueObject>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add(DefaultValueObject.builder().withBitemporalStamp(createPeriod(docId, effectiveDates, i))
                    .withData(EnhancedRandom.random(String.class)).withVersionId(UUID.randomUUID().toString()).build());
        }
        return journal;
    }

    public static LocalDate randomLocalDate(int startYear, int endYear) {
        long minDay = LocalDate.of(startYear, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(endYear, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    public static LocalDate randomLocalDate(LocalDate low, int highYear) {
        long minDay = low.toEpochDay();
        long maxDay = LocalDate.of(highYear, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }
    
    public static LocalDate randomLocalDate(LocalDate low, LocalDate high) {
        long minDay = low.toEpochDay();
        long maxDay = high.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }
    
    public static Instant randomInstant(int startYear, int endYear) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BiFunction<Integer, Integer, Integer> rdm = random::nextInt;
        return LocalDateTime.of(rdm.apply(startYear, endYear), rdm.apply(1, 12), rdm.apply(1, 28), rdm.apply(0, 23),
                rdm.apply(0, 60), rdm.apply(0, 60), rdm.apply(0, 999999999)).toInstant(ZoneOffset.UTC);
    }

    public static void passed() {
        throw new PassedException();
    }

}
