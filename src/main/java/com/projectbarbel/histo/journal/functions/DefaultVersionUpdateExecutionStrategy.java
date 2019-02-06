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

        BitemporalStamp newPrecedingStamp = BitemporalStamp.builder()
                .withActivity(executionContext.getContext().getActivity())
                .withDocumentId(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder()
                        .from(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getEffectiveTime()
                                .from())
                        .until(executionContext.newEffectiveFrom()).build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.getContext().getUser()).build())
                .build();

        BitemporalStamp newSubsequentStamp = BitemporalStamp.builder()
                .withActivity(executionContext.getContext().getActivity())
                .withDocumentId(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getDocumentId())
                .withEffectiveTime(EffectivePeriod.builder().from(executionContext.newEffectiveFrom())
                        .until(((Bitemporal) executionContext.oldVersion()).getBitemporalStamp().getEffectiveTime()
                                .until())
                        .build())
                .withRecordTime(RecordPeriod.builder().createdBy(executionContext.getContext().getUser()).build())
                .build();

        // doing the copies and the proxies
        T newPrecedingVersionBitemporal = generateNewBitemporal(executionContext, newPrecedingStamp);
        T newSupsequentVersionBitemporal = generateNewBitemporal(executionContext, newSubsequentStamp);

        return executionContext.createExecutionResult(newPrecedingVersionBitemporal, newSupsequentVersionBitemporal);
    }

    private T generateNewBitemporal(UpdateExecutionContext<T> executionContext, BitemporalStamp stamp) {
        T newVersion = executionContext.getContext().getMode().copy(executionContext.getContext(),
                executionContext.oldVersion());
        if (newVersion instanceof Bitemporal) { // make sure target and proxy will always sync their stamps
            ((Bitemporal) newVersion).setBitemporalStamp(stamp);
        }
        return executionContext.getContext().getPojoProxyingFunction().apply(newVersion, stamp);
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
