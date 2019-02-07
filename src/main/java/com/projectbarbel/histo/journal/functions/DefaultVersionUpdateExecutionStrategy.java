package com.projectbarbel.histo.journal.functions;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;

import com.projectbarbel.histo.journal.VersionUpdate.UpdateExecutionContext;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class DefaultVersionUpdateExecutionStrategy
        implements Function<UpdateExecutionContext, VersionUpdateResult> {

    @Override
    public VersionUpdateResult apply(UpdateExecutionContext executionContext) {

        BitemporalStamp newPrecedingStamp = BitemporalStamp.builder()
                .withActivity(executionContext.getContext().getActivity())
                .withDocumentId(executionContext.oldVersion().getBitemporalStamp().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder()
                        .from(executionContext.oldVersion().getBitemporalStamp().getEffectiveTime()
                                .from())
                        .until(executionContext.newEffectiveFrom()).build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.getContext().getUser()).build())
                .build();

        BitemporalStamp newSubsequentStamp = BitemporalStamp.builder()
                .withActivity(executionContext.getContext().getActivity())
                .withDocumentId(executionContext.oldVersion().getBitemporalStamp().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder().from(executionContext.newEffectiveFrom())
                        .until(executionContext.oldVersion().getBitemporalStamp().getEffectiveTime()
                                .until())
                        .build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.getContext().getUser()).build())
                .build();

        // doing the copies and the proxies depends on the barbel mode
        Bitemporal newPrecedingVersionBitemporal = executionContext.getContext().getMode().snapshotManagedBitemporal(executionContext.getContext(), executionContext.oldVersion(), newPrecedingStamp);
        Bitemporal newSupsequentVersionBitemporal = executionContext.getContext().getMode().snapshotManagedBitemporal(executionContext.getContext(), executionContext.oldVersion(), newSubsequentStamp);

        return executionContext.createExecutionResult(newPrecedingVersionBitemporal, newSupsequentVersionBitemporal);
    }

    public void setNestedProperty(Object bean, String fieldname, Object value) {
        try {
            PropertyUtils.setNestedProperty(bean, fieldname, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("the property with the name " + fieldname + " in bean of type "
                    + bean.getClass().getName() + " cannot be written", e);
        }
    }

}
