package com.projectbarbel.histo.model;

import java.time.Instant;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;

public final class BitemporalStamp {
	protected final String documentId;
	protected final Instant effectiveFrom;
	protected final Instant effectiveUntil;
	protected final Instant createdAt;
	protected final String createdBy;
	protected final Instant inactivatedAt;
	protected final ObjectState status;
	protected final String inactivatedBy;
	protected final String activity;

	public BitemporalStamp(String documentId, Instant effectiveFrom, Instant effectiveUntil, Instant createdAt,
			String createdBy, Instant inactivatedAt, ObjectState status, String inactivatedBy, String activity) {
		super();
		if (!ObjectUtils.allNotNull(documentId, effectiveFrom, effectiveUntil, createdAt,
			createdBy, inactivatedAt, status, inactivatedBy, activity))
				throw new IllegalArgumentException("Cannot create BitemporalStamp. Must not contain any null value.");
		this.documentId = documentId;
		this.effectiveFrom = effectiveFrom;
		this.effectiveUntil = effectiveUntil;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.inactivatedAt = inactivatedAt;
		this.status = status;
		this.inactivatedBy = inactivatedBy;
		this.activity = activity;
	}

	public BitemporalStamp(BitemporalStamp template) {
		this(template.getDocumentId(), template.getEffectiveFrom(), template.getEffectiveUntil(),
				template.getCreatedAt(), template.getCreatedBy(), template.getInactivatedAt(), template.getStatus(),
				template.getInactivatedBy(), template.getActivity());
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

	public ObjectState getStatus() {
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

}
