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

public class DefaultVersionUpdateExecutionStrategy<T>
        implements Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> {

    @Override
    public VersionUpdateResult<T> apply(UpdateExecutionContext<T> executionContext) {

        BitemporalStamp newPrecedingStamp = BitemporalStamp.builder().withActivity(executionContext.activity())
                .withDocumentId(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder()
                        .from(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getEffectiveTime()
                                .from())
                        .until(executionContext.newEffectiveFrom()).build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.createdBy()).build()).build();

        BitemporalStamp newSubsequentStamp = BitemporalStamp.builder().withActivity(executionContext.activity())
                .withDocumentId(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder().from(executionContext.newEffectiveFrom())
                        .until(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getEffectiveTime()
                                .until())
                        .build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.createdBy()).build()).build();

        T newPrecedingVersion = executionContext.copyFunction().apply((T) executionContext.oldVersion());
        ((Bitemporal) newPrecedingVersion).setBitemporalStamp(newPrecedingStamp);
        T newSubsequentVersion = executionContext.copyFunction().apply((T) executionContext.oldVersion());
        ((Bitemporal) newSubsequentVersion).setBitemporalStamp(newSubsequentStamp);

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
