package com.projectbarbel.histo.model;

import static com.projectbarbel.histo.BarbelHistoHelper.effectiveDateToEffectiveUTCInstant;
import static com.projectbarbel.histo.BarbelHistoHelper.transactionTimeToTransactionInstant;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.annotation.Generated;

/**
 * All instants representing utc time stamps.
 * 
 * @author niklasschlimm
 *
 */
public final class BitemporalStamp {
	protected final String documentId;
	protected final Instant effectiveFrom;
	protected final Instant effectiveUntil;
	protected final Instant createdAt;
	protected final String createdBy;
	protected final Instant inactivatedAt;
	protected final BitemporalObjectState status;
	protected final String inactivatedBy;
	protected final String activity;

    public static BitemporalStamp instance(String documentId, LocalDate effectiveFrom,
            LocalDate effectiveUntil, String activity, String user, ZonedDateTime inactivatedAt, String inactivatedBy) {
        return BitemporalStamp.builder().withActivity(activity).withCreatedAt(Instant.now(Clock.systemUTC()))
                .withCreatedBy(user).withDocumentId(documentId)
                .withEffectiveFrom(effectiveDateToEffectiveUTCInstant(effectiveFrom))
                .withEffectiveUntil(effectiveDateToEffectiveUTCInstant(effectiveUntil))
                .withInactivatedAt(transactionTimeToTransactionInstant(inactivatedAt)).withInactivatedBy(inactivatedBy)
                .withStatus(BitemporalObjectState.ACTIVE).build();
    }

    public BitemporalStamp(BitemporalStamp template) {
        this(template.getDocumentId(), template.getEffectiveFrom(), template.getEffectiveUntil(),
                template.getCreatedAt(), template.getCreatedBy(), template.getInactivatedAt(), template.getStatus(),
                template.getInactivatedBy(), template.getActivity());
    }

	@Generated("SparkTools")
	private BitemporalStamp(Builder builder) {
		this.documentId = builder.documentId;
		this.effectiveFrom = builder.effectiveFrom;
		this.effectiveUntil = builder.effectiveUntil;
		this.createdAt = builder.createdAt;
		this.createdBy = builder.createdBy;
		this.inactivatedAt = builder.inactivatedAt;
		this.status = builder.status;
		this.inactivatedBy = builder.inactivatedBy;
		this.activity = builder.activity;
	}

	private BitemporalStamp(String documentId, Instant effectiveFrom, Instant effectiveUntil, Instant createdAt,
			String createdBy, Instant inactivatedAt, BitemporalObjectState status, String inactivatedBy, String activity) {
		super();
		this.documentId = Objects.requireNonNull(documentId, "documentId");
		this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom");
		this.effectiveUntil = Objects.requireNonNull(effectiveUntil, "effectiveUntil");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
		this.inactivatedAt = Objects.requireNonNull(inactivatedAt, "inactivatedAt");
		this.status = Objects.requireNonNull(status, "status");
		this.inactivatedBy = Objects.requireNonNull(inactivatedBy, "inactivatedBy");
		this.activity = Objects.requireNonNull(activity, "activity");
	}

	public String getDocumentId() {
		return documentId;
	}

	public Instant getEffectiveFrom() {
		return effectiveFrom;
	}

	public Instant getEffectiveUntil() {
		return effectiveUntil;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public Instant getInactivatedAt() {
		return inactivatedAt;
	}

	public BitemporalObjectState getStatus() {
		return status;
	}

	public String getInactivatedBy() {
		return inactivatedBy;
	}

	public String getActivity() {
		return activity;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof BitemporalStamp)) {
			return false;
		}
		BitemporalStamp abstractValueObject = (BitemporalStamp) o;
		return Objects.equals(documentId, abstractValueObject.getDocumentId())
				&& Objects.equals(effectiveFrom, abstractValueObject.getEffectiveFrom())
				&& Objects.equals(effectiveUntil, abstractValueObject.getEffectiveUntil())
				&& Objects.equals(createdAt, abstractValueObject.getCreatedAt())
				&& Objects.equals(createdBy, abstractValueObject.getCreatedBy())
				&& Objects.equals(inactivatedAt, abstractValueObject.getInactivatedAt())
				&& Objects.equals(status, abstractValueObject.getStatus())
				&& Objects.equals(inactivatedBy, abstractValueObject.getInactivatedBy())
				&& Objects.equals(activity, abstractValueObject.getActivity());
	}

	@Override
	public int hashCode() {
		return Objects.hash(documentId, effectiveFrom, effectiveUntil, createdAt, createdBy, inactivatedAt, status,
				inactivatedBy, activity);
	}

	@Override
	public String toString() {
		return "BitemporalStamp [documentId=" + documentId + ", effectiveFrom=" + effectiveFrom + ", effectiveUntil="
				+ effectiveUntil + ", createdAt=" + createdAt + ", createdBy=" + createdBy + ", inactivatedAt="
				+ inactivatedAt + ", status=" + status + ", inactivatedBy=" + inactivatedBy + ", activity=" + activity
				+ "]";
	}

	/**
	 * Creates builder to build {@link BitemporalStamp}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link BitemporalStamp}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String documentId;
		private Instant effectiveFrom;
		private Instant effectiveUntil;
		private Instant createdAt;
		private String createdBy;
		private Instant inactivatedAt;
		private BitemporalObjectState status;
		private String inactivatedBy;
		private String activity;

		private Builder() {
		}

		public Builder withDocumentId(String documentId) {
			this.documentId = documentId;
			return this;
		}

		public Builder withEffectiveFrom(Instant effectiveFrom) {
			this.effectiveFrom = effectiveFrom;
			return this;
		}

		public Builder withEffectiveUntil(Instant effectiveUntil) {
			this.effectiveUntil = effectiveUntil;
			return this;
		}

		public Builder withCreatedAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder withCreatedBy(String createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public Builder withInactivatedAt(Instant inactivatedAt) {
			this.inactivatedAt = inactivatedAt;
			return this;
		}

		public Builder withStatus(BitemporalObjectState status) {
			this.status = status;
			return this;
		}

		public Builder withInactivatedBy(String inactivatedBy) {
			this.inactivatedBy = inactivatedBy;
			return this;
		}

		public Builder withActivity(String activity) {
			this.activity = activity;
			return this;
		}

		public BitemporalStamp build() {
			return new BitemporalStamp(this);
		}
	}

}
