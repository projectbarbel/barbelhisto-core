package com.projectbarbel.histo;

import com.projectbarbel.histo.journal.functions.BarbelProxy;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;

public abstract class BarbelMode {

    public static BarbelMode POJO = new PojoMode();
    public static BarbelMode BITEMPORAL = new BitemporalMode();

    public abstract <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp);

    public abstract <T> T copy(BarbelHistoContext<T> context, T pojo);

    public static class PojoMode extends BarbelMode {

        @Override
        public <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp) {
            return context.getPojoProxyingFunction().apply(newVersion, stamp);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T copy(BarbelHistoContext<T> context, T pojo) {
            return context.getPojoCopyFunction().apply(((BarbelProxy<T>) pojo).getTarget());
        }

    }

    public static class BitemporalMode extends BarbelMode {

        @Override
        public <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp) {
            ((BitemporalVersion) newVersion).setBitemporalStamp(stamp);
            return newVersion;
        }

        @Override
        public <T> T copy(BarbelHistoContext<T> context, T pojo) {
            return context.getPojoCopyFunction().apply(pojo);
        }

    }

}