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

import org.javers.common.collections.Arrays;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.journal.functions.DefaultIDGenerator;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
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
                return EffectivePeriod.builder().from(effectiveFrom)
                        .until(randomLocalDate(effectiveFrom.plusDays(1), 2020)).build();
            }
        }).randomize(RecordPeriod.class, new Supplier<RecordPeriod>() {

            @Override
            public RecordPeriod get() {
                return RecordPeriod.builder().build();
            }
        }).randomize(
                new FieldDefinition<BitemporalStamp, Supplier>("versionIdGenerator", Supplier.class, BitemporalStamp.class),
                new Supplier<DefaultIDGenerator>() {

                    @Override
                    public DefaultIDGenerator get() {
                        return new DefaultIDGenerator();
                    }
                }).randomize(
                        new FieldDefinition<BitemporalStamp, Supplier>("documentIdGenerator", Supplier.class, BitemporalStamp.class),
                        new Supplier<DefaultIDGenerator>() {

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
        return BitemporalStamp.builder().withDocumentId(docId)
                .withEffectiveTime(effectiveDates.size() - 1 == listPointer
                        ? EffectivePeriod.builder().from(effectiveDates.get(listPointer)).toInfinite().build()
                        : EffectivePeriod.builder().from(effectiveDates.get(listPointer))
                                .until(effectiveDates.get(listPointer + 1)).build())
                .withRecordTime(RecordPeriod.builder().build()).build();
    }

    @SuppressWarnings("unchecked")
    public static <T> IndexedCollection<T> generateJournalOfDefaultValueObjects(String docId,
            List<LocalDate> effectiveDates) {
        IndexedCollection<T> journal = new ConcurrentIndexedCollection<T>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add((T)DefaultDocument.builder().withBitemporalStamp(createPeriod(docId, effectiveDates, i))
                    .withData(EnhancedRandom.random(String.class)).build());
        }
        return journal;
    }

    @SuppressWarnings("unchecked")
    public static <T> IndexedCollection<T> asIndexedCollection(T... objects){
        ConcurrentIndexedCollection<T> collection = new ConcurrentIndexedCollection<>();
        Arrays.asList(objects).stream().forEach(d->collection.add((T)d));
        return collection;
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
