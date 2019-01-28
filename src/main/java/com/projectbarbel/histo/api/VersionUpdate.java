package com.projectbarbel.histo.api;

import java.time.LocalDate;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.projectbarbel.histo.functions.DefaultPojoCopier;
import com.projectbarbel.histo.functions.DefaultUpdateExectionStrategy;
import com.projectbarbel.histo.functions.ValidateEffectiveDate;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;

public class VersionUpdate {

    private final Bitemporal<?> oldVersion;
    private Bitemporal<?> newPrecedingVersion;
    private Bitemporal<?> newSubsequentVersion;
    private LocalDate newEffectiveDate;
    private String activity = "SYSTEM_ACTIVITY";
    private String createdBy = "SYSTEM";
    private BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copyFunction = new DefaultPojoCopier();

    public static class VersionUpdateResult {

        private VersionUpdate update;
        
        private VersionUpdateResult(VersionUpdate update, Bitemporal<?> newPrecedingVersion, Bitemporal<?> subSequentVersion) {
            update.newPrecedingVersion = newPrecedingVersion;
            update.newSubsequentVersion = subSequentVersion;
            this.update = update;
        }

        public Bitemporal<?> oldVersion() {
            return update.oldVersion;
        }

        public Bitemporal<?> newPrecedingVersion() {
            return update.newPrecedingVersion;
        }

        public Bitemporal<?> newSubsequentVersion() {
            return update.newSubsequentVersion;
        }
        
    }

    public static class VersionUpdateExecutionBuilder {
        private final VersionUpdate update;
        private BiPredicate<Bitemporal<?>, LocalDate> effectiveDateValidationFuction = new ValidateEffectiveDate();
        private Function<UpdateExecutionContext, VersionUpdateResult> updateExecutionFunction = new DefaultUpdateExectionStrategy();

        private VersionUpdateExecutionBuilder(Bitemporal<?> oldVersion) {
            update = new VersionUpdate(oldVersion);
        }

        public VersionUpdateExecutionBuilder effectiveFrom(LocalDate newEffectiveFrom) {
            if (!effectiveDateValidationFuction.test(update.oldVersion, newEffectiveFrom))
                throw new IllegalArgumentException("new effective date is not valid: " + newEffectiveFrom
                        + " - old version: " + update.oldVersion.toString());
            update.newEffectiveDate = newEffectiveFrom;
            return this;
        }

        public VersionUpdateExecutionBuilder activity(String activity) {
            update.activity = activity;
            return this;
        }

        public VersionUpdateExecutionBuilder createdBy(String createdBy) {
            update.createdBy = createdBy;
            return this;
        }

        public VersionUpdateExecutionBuilder customCopyFunction(
                BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copyFunction) {
            update.copyFunction = copyFunction;
            return this;
        }

        public VersionUpdateExecutionBuilder customEffectiveDateValidationFunction(
                BiPredicate<Bitemporal<?>, LocalDate> function) {
            effectiveDateValidationFuction = function;
            return this;
        }

        public VersionUpdateResult execute() {
            return updateExecutionFunction.apply(new UpdateExecutionContext(update));
        }

    }

    public static class UpdateExecutionContext {
        private VersionUpdate update;

        private UpdateExecutionContext(VersionUpdate update) {
            super();
            this.update = update;
        }

        public Bitemporal<?> oldVersion() {
            return update.oldVersion;
        }

        public BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copyFunction() {
            return update.copyFunction;
        }

        public LocalDate newEffectiveFrom() {
            return update.newEffectiveDate;
        }

        public String activity() {
            return update.activity;
        }

        public String createdBy() {
            return update.createdBy;
        }
        
        public VersionUpdateResult createExecutionResult(Bitemporal<?> newPrecedingVersion, Bitemporal<?> newSubsequentVersion) {
            return new VersionUpdateResult(update, newPrecedingVersion, newSubsequentVersion);
        }
        
    }

    @SuppressWarnings("unchecked")
    public static <T extends VersionUpdateExecutionBuilder> T of(Bitemporal<?> oldVersion) {
        return (T) new VersionUpdateExecutionBuilder(oldVersion);
    }

    private VersionUpdate(Bitemporal<?> bitemporal) {
        oldVersion = bitemporal;
        newEffectiveDate = bitemporal.getEffectiveFrom();
    }

}
