package com.projectbarbel.histo.model;

import com.projectbarbel.histo.BarbelHistoContext;

public interface HistoContextAware {

    BarbelHistoContext context();
    void withContext(BarbelHistoContext ctx);
    
}
