package com.projectbarbel.histo.journal;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.journal.functions.ValidateEffectiveDate;
import com.projectbarbel.histo.model.Bitemporal;

public final class VersionUpdate {

    public enum UpdateState {
        PREPARATION(() -> {
        }, () -> Validate.validState(false, "this method is not allowed in state PTREPARATION")),
        EXECUTED(() -> Validate.validState(false, "this method is not allowed in state EXECUTED"), () -> {
        });
        private Runnable inputSetter;
        private Runnable outputGetter;

        private UpdateState(Runnable inputSetter, Runnable outputGetter) {
            this.inputSetter = inputSetter;
            this.outputGetter = outputGetter;
        }

        public <T> T set(T value) {
            inputSetter.run();
            return value;
        }

        public <T> T get(T value) {
            outputGetter.run();
            return value;
        }
    }

    private final Bitemporal oldVersion;
    private Bitemporal newPrecedingVersion;
    private Bitemporal newSubsequentVersion;
    private LocalDate newEffectiveDate;
    private LocalDate newEffectiveUntil;
    private UpdateState state = UpdateState.PREPARATION;
    private BarbelHistoContext context;
    private VersionUpdateResult result;

    private VersionUpdate(BarbelHistoContext context, Bitemporal bitemporal) {
        oldVersion = Objects.requireNonNull(bitemporal, "bitemporal object must not be null");
        newEffectiveDate = Objects.requireNonNull(bitemporal.getBitemporalStamp().getEffectiveTime().from(),
                "the bitemporal passed must not contain null value on effective from");
        newEffectiveUntil = Objects.requireNonNull(bitemporal.getBitemporalStamp().getEffectiveTime().until(),
                "the bitemporal passed must not contain null value on effective until");
        this.context = context;
    }

    public static VersionUpdate of(BarbelHistoContext context, Bitemporal document) {
        Validate.isTrue(document instanceof Bitemporal, "only bitemporal objects can be the source for an update");
        return new VersionUpdate(context, document);
    }

    public VersionUpdateExecutionBuilder prepare() {
        return new VersionUpdateExecutionBuilder(this);
    }

    public VersionUpdateResult execute() {
        result = state
                .set(context.getVersionUpdateExecutionStrategy().apply(new UpdateExecutionContext(context, this)));
        state = UpdateState.EXECUTED;
        return result;
    }

    public VersionUpdateResult result() {
        return state.get(result);
    }

    public boolean isDone() {
        return state.equals(UpdateState.EXECUTED);
    }

    public static class VersionUpdateResult {

        private VersionUpdate update;

        private VersionUpdateResult(VersionUpdate update, Bitemporal newPrecedingVersion,
                Bitemporal subSequentVersion) {
            update.newPrecedingVersion = newPrecedingVersion;
            update.newSubsequentVersion = subSequentVersion;
            this.update = update;
        }

        public Bitemporal oldVersion() {
            return update.oldVersion;
        }

        public Bitemporal newPrecedingVersion() {
            return update.newPrecedingVersion;
        }

        public Bitemporal newSubsequentVersion() {
            return update.newSubsequentVersion;
        }

        public LocalDate effectiveFrom() {
            return update.newEffectiveDate;
        }

        public LocalDate effectiveUntil() {
            return update.newEffectiveUntil;
        }

        public void setNewSubsequentVersion(Bitemporal newSubsequentVersion) {
            Validate.isTrue(newSubsequentVersion.getClass().equals(update.newSubsequentVersion.getClass()),
                    "new subsequent version must be of the same type as preceding version");
            Validate.isTrue(
                    newSubsequentVersion.getBitemporalStamp().getDocumentId()
                            .equals(update.newSubsequentVersion.getBitemporalStamp().getDocumentId()),
                    "only objects with the same document is can be predecessor and successor in an update");
            Validate.isTrue(
                    newSubsequentVersion.getBitemporalStamp().getEffectiveTime().from()
                            .equals(update.newPrecedingVersion.getBitemporalStamp().getEffectiveTime().until()),
                    "custom subsequent version must have effective from equal to effective until of preseding version");
            update.newSubsequentVersion = newSubsequentVersion;
        }

    }

    public static class VersionUpdateExecutionBuilder {
        private final VersionUpdate update;
        private BiPredicate<Bitemporal, LocalDate> effectiveDateValidationFuction = new ValidateEffectiveDate();

        private VersionUpdateExecutionBuilder(VersionUpdate update) {
            this.update = update;
        }

        public VersionUpdateExecutionBuilder effectiveFrom(LocalDate newEffectiveFrom) {
            if (!effectiveDateValidationFuction.test(update.oldVersion, newEffectiveFrom))
                throw new IllegalArgumentException("new effective date is not valid: " + newEffectiveFrom
                        + " - old version: " + update.oldVersion.toString());
            update.newEffectiveDate = update.state.set(newEffectiveFrom);
            return this;
        }

        public VersionUpdateExecutionBuilder untilInfinite() {
            update.newEffectiveUntil = update.state.set(BarbelHistoContext.getInfiniteDate());
            return this;
        }

        public VersionUpdateExecutionBuilder until(LocalDate newEffectiveUntil) {
            update.newEffectiveUntil = update.state.set(newEffectiveUntil);
            return this;
        }

        public VersionUpdateResult execute() {
            return update.execute();
        }

        public VersionUpdate get() {
            return update;
        }

    }

    public static class UpdateExecutionContext {
        private final VersionUpdate update;
        private final BarbelHistoContext context;

        private UpdateExecutionContext(BarbelHistoContext context, VersionUpdate update) {
            super();
            this.update = update;
            this.context = context;
        }

        public BarbelHistoContext getContext() {
            return context;
        }

        public Bitemporal oldVersion() {
            return update.oldVersion;
        }

        public LocalDate newEffectiveFrom() {
            return update.newEffectiveDate;
        }

        public VersionUpdateResult createExecutionResult(Bitemporal newPrecedingVersion,
                Bitemporal newSubsequentVersion) {
            return new VersionUpdateResult(update, newPrecedingVersion, newSubsequentVersion);
        }

    }

}
