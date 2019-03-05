package org.projectbarbel.histo.suite;

import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.suite.context.BTTestContext;
import org.projectbarbel.histo.suite.context.BTTestContextStandard;

public class BTExecutionContext {
    private BTTestContext testContext = new BTTestContextStandard();
    public static final BTExecutionContext INSTANCE = new BTExecutionContext();
    private BTExecutionContext() {
    }
    public BarbelHistoBuilder barbel(Class<?> type) {
        return getTestContext().contextFunction().apply(type);
    }
    public void clearResources() {
        testContext.clearResources();
    }
    
    public BTTestContext getTestContext() {
        return testContext;
    }
    
    public void setTestContext(BTTestContext testContext) {
        this.testContext = testContext;
    }
    
}
