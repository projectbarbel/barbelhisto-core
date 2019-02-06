package com.projectbarbel.histo;

import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;

public abstract class BarbelMode {

    public static BarbelMode PROXY = new ProxyMode();
    public static BarbelMode BITEMPORALVERSION = new ProxyMode();
    
    public abstract <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp);
    
    public static class ProxyMode extends BarbelMode {

        @Override
        public <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp) {
            return context.getPojoProxyingFunction().apply(newVersion, stamp);
        }

    }
    
    public static class BitemporalVersionMode extends BarbelMode {
        
        @Override
        public <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp) {
            ((BitemporalVersion)newVersion).setBitemporalStamp(stamp);
            return newVersion;
        }
        
    }
    
    
    
    
}