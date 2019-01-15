package com.projectbarbel.histo.model;

import java.time.Instant;
import java.util.Objects;

public class DefaultValueObject extends AbstractValueObject<DefaultValueObject> {

	private final String data;

	public DefaultValueObject(String documentId, Instant effectiveFrom, Instant effectiveUntil, Instant createdAt,
			String createdBy, Instant inactivatedAt, ObjectState status, String inactivatedBy, String activity,
			String data) {
		super(documentId, effectiveFrom, effectiveUntil, createdAt, createdBy, inactivatedAt, status, inactivatedBy,
				activity);
		this.data = data;
	}

	public DefaultValueObject(DefaultValueObject template) {
		super(template.getDocumentId(), template.getEffectiveFrom(), template.getEffectiveUntil(),
				template.getCreatedAt(), template.getCreatedBy(), template.getInactivatedAt(), template.getStatus(),
				template.getInactivatedBy(), template.getActivity());
		this.data = template.getData();
	}

	public String getData() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof AbstractValueObject)) {
			return false;
		}
		DefaultValueObject defaultValueObject = (DefaultValueObject) o;
		return Objects.equals(documentId, defaultValueObject.getDocumentId())
				&& Objects.equals(effectiveFrom, defaultValueObject.getEffectiveFrom())
				&& Objects.equals(effectiveUntil, defaultValueObject.getEffectiveUntil())
				&& Objects.equals(createdAt, defaultValueObject.getCreatedAt())
				&& Objects.equals(createdBy, defaultValueObject.getCreatedBy())
				&& Objects.equals(inactivatedAt, defaultValueObject.getInactivatedAt())
				&& Objects.equals(status, defaultValueObject.getStatus())
				&& Objects.equals(inactivatedBy, defaultValueObject.getInactivatedBy())
				&& Objects.equals(activity, defaultValueObject.getActivity())
				&& Objects.equals(data, defaultValueObject.getData());
	}

	@Override
	public int hashCode() {
		return Objects.hash(documentId, effectiveFrom, effectiveUntil, createdAt, createdBy, inactivatedAt, status,
				inactivatedBy, activity, data);
	}

	@Override
	public DefaultValueObject get() {
		return new DefaultValueObject(this);
	}

}
