package org.projectbarbel.histo.suite.extensions;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.context.BTTestContext;
import org.projectbarbel.histo.suite.context.BTTestContextStandard;

public class BTTestStandard implements BeforeAllCallback, AfterAllCallback {

    private BTTestContext previousContext;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        previousContext = BTExecutionContext.INSTANCE.getTestContext();
        if (previousContext == null) {
            BTExecutionContext.INSTANCE.setTestContext(new BTTestContextStandard());
        } else {
            BTExecutionContext.INSTANCE.getTestContext().clearResources();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        BTExecutionContext.INSTANCE.getTestContext().clearResources();
        BTExecutionContext.INSTANCE.setTestContext(previousContext);
    }

}
