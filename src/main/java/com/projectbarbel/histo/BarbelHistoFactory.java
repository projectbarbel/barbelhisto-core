package com.projectbarbel.histo;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;

public class BarbelHistoFactory<T> {

    private BarbelHistoContext<T> context;

    public BarbelHistoFactory(BarbelHistoContext<T> context) {
        this.context = context;
    }

    public static <T> VersionUpdate<T> createDefaultVersionUpdate(BarbelHistoContext<T> context, T currentVersion) {
        Validate.isTrue(currentVersion instanceof Bitemporal, "only bitemporals are valid inputs");
        return VersionUpdate.<T>of(context, currentVersion);
    }

    public BarbelHistoFactory<T> create(BarbelHistoContext<T> context) {
        return new BarbelHistoFactory<T>(context);
    }

    public BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> createJournalUpdateStrategy() {
        return context.getJournalUpdateStrategy().apply(context);
    }

    public VersionUpdate<T> createVersionUpdate(T currentVersion) {
        Validate.isTrue(currentVersion instanceof Bitemporal, "only type Bitemporal allowed as oldVersion");
        return VersionUpdate.of(context, currentVersion);
    }

    public static <T> String prettyPrint(T bitemporal) {
        Bitemporal object = (Bitemporal) bitemporal;
        return String.format("|%1$-40s|%2$-15tF|%3$-16tF|%4$-8s|%5$-21s|%6$-23s|%7$-21s|%8$-23s|",
                object.getBitemporalStamp().getVersionId(), object.getBitemporalStamp().getEffectiveTime().from(),
                object.getBitemporalStamp().getEffectiveTime().until(),
                object.getBitemporalStamp().getRecordTime().getState().name(),
                object.getBitemporalStamp().getRecordTime().getCreatedBy().substring(0,
                        Math.min(object.getBitemporalStamp().getRecordTime().getCreatedBy().length(), 20)),
                DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm:ss")
                        .format(object.getBitemporalStamp().getRecordTime().getCreatedAt()),
                object.getBitemporalStamp().getRecordTime().getInactivatedBy().substring(0,
                        Math.min(object.getBitemporalStamp().getRecordTime().getCreatedBy().length(), 20)),
                object.getBitemporalStamp().getRecordTime().getInactivatedAt());
    }

}
