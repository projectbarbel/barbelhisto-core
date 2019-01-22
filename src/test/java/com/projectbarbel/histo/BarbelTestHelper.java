package com.projectbarbel.histo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.projectbarbel.histo.model.BitemporalStamp;

public class BarbelTestHelper {
    public static List<BitemporalStamp> generateJournal(String docId, List<LocalDate> effectiveDates) {
        List<BitemporalStamp> journal = new ArrayList<BitemporalStamp>();
        for (int i = 0; i < effectiveDates.size(); i++) {
            journal.add(BitemporalStamp.instance(docId, effectiveDates.get(i),
                    effectiveDates.size() - 1 == i ? null : effectiveDates.get(i + 1), "SYSTEM_PROCESS", "SYSTEM_USER",
                    null, null));
        }
        return journal;
    }
}
