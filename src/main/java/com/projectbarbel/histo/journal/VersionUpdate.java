package com.projectbarbel.histo.journal;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.journal.functions.ValidateEffectiveDate;
import com.projectbarbel.histo.model.Bitemporal;

public final class VersionUpdate<T> {

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

    private final T oldVersion;
    private T newPrecedingVersion;
    private T newSubsequentVersion;
    private LocalDate newEffectiveDate;
    private LocalDate newEffectiveUntil;
    private UpdateState state = UpdateState.PREPARATION;
    private BarbelHistoContext<T> context;
    private VersionUpdateResult<T> result;

    private VersionUpdate(BarbelHistoContext<T> context, T bitemporal) {
        oldVersion = Objects.requireNonNull(bitemporal, "bitemporal object must not be null");
        newEffectiveDate = Objects.requireNonNull(
                ((Bitemporal) bitemporal).getBitemporalStamp().getEffectiveTime().from(),
                "the bitemporal passed must not contain null value on effective from");
        newEffectiveUntil = Objects.requireNonNull(
                ((Bitemporal) bitemporal).getBitemporalStamp().getEffectiveTime().until(),
                "the bitemporal passed must not contain null value on effective until");
        this.context = context;
    }

    public static <T> VersionUpdate<T> of(BarbelHistoContext<T> context, T document) {
        Validate.isTrue(document instanceof Bitemporal, "only bitemporal objects can be the source for an update");
        return new VersionUpdate<T>(context, document);
    }

    public VersionUpdateExecutionBuilder<T> prepare() {
        return new VersionUpdateExecutionBuilder<T>(this);
    }

    @SuppressWarnings("unchecked")
    public <O> VersionUpdateResult<O> execute() {
        result = state.set(context.getVersionUpdateExecutionStrategy().apply(new UpdateExecutionContext<T>(context, this)));
        state = UpdateState.EXECUTED;
        return (VersionUpdateResult<O>) result;
    }

    public VersionUpdateResult<T> result() {
        return state.get(result);
    }

    public boolean isDone() {
        return state.equals(UpdateState.EXECUTED);
    }

    public static class VersionUpdateResult<T> {

        private VersionUpdate<T> update;

        private VersionUpdateResult(VersionUpdate<T> update, T newPrecedingVersion, T subSequentVersion) {
            update.newPrecedingVersion = newPrecedingVersion;
            update.newSubsequentVersion = subSequentVersion;
            this.update = update;
        }

        public T oldVersion() {
            return update.oldVersion;
        }

        public T newPrecedingVersion() {
            return update.newPrecedingVersion;
        }

        public T newSubsequentVersion() {
            return update.newSubsequentVersion;
        }

        public LocalDate effectiveFrom() {
            return update.newEffectiveDate;
        }

        public LocalDate effectiveUntil() {
            return update.newEffectiveUntil;
        }

        public void setNewSubsequentVersion(T newSubsequentVersion) {
            Validate.isTrue(newSubsequentVersion.getClass().equals(update.newSubsequentVersion.getClass()),
                    "new subsequent version must be of the same type as preceding version");
            Validate.isTrue(
                    ((Bitemporal) newSubsequentVersion).getBitemporalStamp().getDocumentId()
                            .equals(((Bitemporal) update.newSubsequentVersion).getBitemporalStamp().getDocumentId()),
                    "only objects with the same document is can be predecessor and successor in an update");
            Validate.isTrue(
                    ((Bitemporal) newSubsequentVersion).getBitemporalStamp().getEffectiveTime().from().equals(
                            ((Bitemporal) update.newPrecedingVersion).getBitemporalStamp().getEffectiveTime().until()),
                    "custom subsequent version must have effective from equal to effective until of preseding version");
            update.newSubsequentVersion = newSubsequentVersion;
        }

    }

    public static class VersionUpdateExecutionBuilder<T> {
        private final VersionUpdate<T> update;
        private BiPredicate<Bitemporal, LocalDate> effectiveDateValidationFuction = new ValidateEffectiveDate();

        private VersionUpdateExecutionBuilder(VersionUpdate<T> update) {
            this.update = update;
        }

        public VersionUpdateExecutionBuilder<T> effectiveFrom(LocalDate newEffectiveFrom) {
            if (!effectiveDateValidationFuction.test((Bitemporal) update.oldVersion, newEffectiveFrom))
                throw new IllegalArgumentException("new effective date is not valid: " + newEffectiveFrom
                        + " - old version: " + update.oldVersion.toString());
            update.newEffectiveDate = update.state.set(newEffectiveFrom);
            return this;
        }

        public VersionUpdateExecutionBuilder<T> untilInfinite() {
            update.newEffectiveUntil = update.state.set(BarbelHistoContext.getInfiniteDate());
            return this;
        }

        public VersionUpdateExecutionBuilder<T> until(LocalDate newEffectiveUntil) {
            update.newEffectiveUntil = update.state.set(newEffectiveUntil);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <O> VersionUpdateResult<O> execute() {
            return (VersionUpdateResult<O>) update.execute();
        }

        public VersionUpdate<T> get() {
            return update;
        }

    }

    public static class UpdateExecutionContext<T> {
        private final VersionUpdate<T> update;
        private final BarbelHistoContext<T> context;

        private UpdateExecutionContext(BarbelHistoContext<T> context, VersionUpdate<T> update) {
            super();
            this.update = update;
            this.context = context;
        }
        
        public BarbelHistoContext<T> getContext() {
            return context;
        }

        public T oldVersion() {
            return update.oldVersion;
        }

        public LocalDate newEffectiveFrom() {
            return update.newEffectiveDate;
        }

        public VersionUpdateResult<T> createExecutionResult(T newPrecedingVersion, T newSubsequentVersion) {
            return new VersionUpdateResult<T>(update, newPrecedingVersion, newSubsequentVersion);
        }

    }

}
