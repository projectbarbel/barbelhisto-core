package com.projectbarbel.histo.model;

import java.util.Objects;

public class DefaultValueObject implements Bitemporal {

	private final BitemporalStamp bitemporalStamp;
	private final String data;

	public DefaultValueObject(BitemporalStamp stamp, String data) {
		this.bitemporalStamp = stamp;
		this.data = data;
	}

	public DefaultValueObject(DefaultValueObject template) {
		this.bitemporalStamp = template.getBitemporalStamp();
		this.data = template.getData();
	}

	public String getData() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof DefaultValueObject)) {
			return false;
		}
		DefaultValueObject defaultValueObject = (DefaultValueObject) o;
		return Objects.equals(data, defaultValueObject.getData())
				&& Objects.equals(bitemporalStamp, defaultValueObject.getBitemporalStamp());
	}

	@Override
	public int hashCode() {
		return Objects.hash(bitemporalStamp, data);
	}

	@Override
	public BitemporalStamp getBitemporalStamp() {
		return bitemporalStamp;
	}

}
