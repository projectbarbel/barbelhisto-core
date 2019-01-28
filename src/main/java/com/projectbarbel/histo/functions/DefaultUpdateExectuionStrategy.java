package com.projectbarbel.histo.functions;

import java.util.function.Function;

import com.projectbarbel.histo.api.VersionUpdate.UpdateExecutionContext;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class DefaultUpdateExectuionStrategy implements Function<UpdateExecutionContext, VersionUpdateResult> {

    @Override
    public VersionUpdateResult apply(UpdateExecutionContext executionContext) {
        BitemporalStamp newPrecedingStamp = BitemporalStamp.of(
                executionContext.activity(), executionContext.oldVersion().getDocumentId(), EffectivePeriod.create()
                        .from(executionContext.oldVersion().getEffectiveFromInstant()).until(executionContext.newEffectiveFrom()),
                RecordPeriod.create(executionContext.createdBy()));
        BitemporalStamp newSubsequentStamp = BitemporalStamp.of(
                executionContext.activity(), executionContext.oldVersion().getDocumentId(), EffectivePeriod.create()
                        .from(executionContext.newEffectiveFrom()).until(executionContext.oldVersion().getEffectiveUntilInstant()),
                RecordPeriod.create(executionContext.createdBy()));
        Bitemporal<?> newPrecedingVersion = executionContext.copyFunction().apply(executionContext.oldVersion(), newPrecedingStamp);
        Bitemporal<?> newSubsequentVersion = executionContext.copyFunction().apply(executionContext.oldVersion(), newSubsequentStamp);
        executionContext.oldVersion().inactivate();
        return executionContext.createExecutionResult(newPrecedingVersion, newSubsequentVersion);
    }

}
