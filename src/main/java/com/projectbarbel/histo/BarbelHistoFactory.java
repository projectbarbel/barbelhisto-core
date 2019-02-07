package com.projectbarbel.histo;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class BarbelHistoFactory {

    private BarbelHistoContext context;

    public BarbelHistoFactory(BarbelHistoContext context) {
        this.context = context;
    }

    public static VersionUpdate createDefaultVersionUpdate(BarbelHistoContext context, Bitemporal currentVersion) {
        Validate.isTrue(currentVersion instanceof Bitemporal, "only bitemporals are valid inputs");
        return VersionUpdate.of(context, currentVersion);
    }

    public BarbelHistoFactory create(BarbelHistoContext context) {
        return new BarbelHistoFactory(context);
    }

    public BiFunction<DocumentJournal, VersionUpdateResult, List<Object>> createJournalUpdateStrategy() {
        return context.getJournalUpdateStrategy().apply(context);
    }

    public VersionUpdate createVersionUpdate(Bitemporal currentVersion) {
        Validate.isTrue(currentVersion instanceof Bitemporal, "only type Bitemporal allowed as oldVersion");
        return VersionUpdate.of(context, currentVersion);
    }

}
