package com.projectbarbel.histo.functions.update;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;

import com.projectbarbel.histo.api.VersionUpdate.UpdateExecutionContext;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class DefaultUpdateExectuionStrategy<T extends Bitemporal<?>>
        implements Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> {

    @Override
    public VersionUpdateResult<T> apply(UpdateExecutionContext<T> executionContext) {
        BitemporalStamp newPrecedingStamp = BitemporalStamp.builder().withActivity(executionContext.activity())
                .withDocumentId(executionContext.oldVersion().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder().from(executionContext.oldVersion().getEffectiveFrom())
                        .until(executionContext.newEffectiveFrom()).build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.createdBy()).build()).build();
        BitemporalStamp newSubsequentStamp = BitemporalStamp.builder().withActivity(executionContext.activity())
                .withDocumentId(executionContext.oldVersion().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder().from(executionContext.newEffectiveFrom())
                        .until(executionContext.oldVersion().getEffectiveUntil()).build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.createdBy()).build()).build();
        T newPrecedingVersion = executionContext.copyFunction().apply(executionContext.oldVersion(), newPrecedingStamp);
        T newSubsequentVersion = executionContext.copyFunction().apply(executionContext.oldVersion(),
                newSubsequentStamp);
        executionContext.propertyUpdates().keySet().stream()
                .forEach((k) -> setNestedProperty(newSubsequentVersion, k, executionContext.propertyUpdates().get(k)));
        return executionContext.createExecutionResult(newPrecedingVersion, newSubsequentVersion);
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
