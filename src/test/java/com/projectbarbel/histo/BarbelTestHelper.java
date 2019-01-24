package com.projectbarbel.histo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalObjectState;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultValueObject;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelTestHelper {
    public static List<BitemporalStamp> generateListOfBitemporals(String docId, List<LocalDate> effectiveDates) {
        List<BitemporalStamp> journal = new ArrayList<BitemporalStamp>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add(BitemporalStamp.instance(docId, effectiveDates.get(i),
                    effectiveDates.size() - 1 == i ? Bitemporal.EffectivePeriod.INFINITE : effectiveDates.get(i + 1), "SYSTEM_PROCESS", "SYSTEM_USER",
                    BitemporalObjectState.ACTIVE,
                    ZonedDateTime.ofInstant(BitemporalStamp.NOT_INACTIVATED, ZoneId.of("Z")), BitemporalStamp.NOBODY));
        }
        return journal;
    }

    public static List<DefaultValueObject> generateJournalOfDefaultValueObjects(String docId,
            List<LocalDate> effectiveDates) {
        List<DefaultValueObject> journal = new ArrayList<DefaultValueObject>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add(DefaultValueObject.builder().withBitemporalStamp(BitemporalStamp.instance(docId,
                    effectiveDates.get(i), effectiveDates.size() - 1 == i ? Bitemporal.EffectivePeriod.INFINITE : effectiveDates.get(i + 1),
                    "SYSTEM_PROCESS", "SYSTEM_USER", BitemporalObjectState.ACTIVE,
                    ZonedDateTime.ofInstant(BitemporalStamp.NOT_INACTIVATED, ZoneId.of("Z")), BitemporalStamp.NOBODY))
                    .withData(EnhancedRandom.random(String.class)).withVersionId(UUID.randomUUID().toString()).build());
        }
        return journal;
    }

    public static DefaultValueObject createCopyOfDefaultValueObjectWithEffectiveDateAndData(DefaultValueObject template,
            LocalDate effectiveDate, String data) {
        return null;
    }
}
