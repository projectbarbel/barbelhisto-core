package org.projectbarbel.histo;

import org.projectbarbel.histo.AbstractBarbelMode.BitemporalMode;
import org.projectbarbel.histo.AbstractBarbelMode.PojoMode;

public enum BarbelMode {

	POJO(new PojoMode()), BITEMPORAL(new BitemporalMode());
	private AbstractBarbelMode mode;

	private BarbelMode(AbstractBarbelMode mode) {
		this.mode = mode;
	}

	public AbstractBarbelMode get() {
		return mode;
	}
}
