package com.projectbarbel.histo;

public class BarbelHistoCore implements BarbelHisto {

    private final BarbelHistoContext context;
    
    public BarbelHistoCore(BarbelHistoContext context) {
        this.context = context;
    }
    
    @Override
    public void save(Object currentVersion) {
        
    }

    public BarbelHistoContext getContext() {
        return context;
    }

}
