package org.projectbarbel.histo.suite.context;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.projectbarbel.histo.suite.BTExecutionContext;

public class BTTestPersitenceListenerOnly implements ExecutionCondition, AfterAllCallback, BeforeAllCallback {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (BTExecutionContext.INSTANCE.getTestContext() instanceof BTTestContextPersistenceListener)
            return ConditionEvaluationResult.enabled("runs for listener context: "
                    + BTExecutionContext.INSTANCE.getTestContext().getClass().getName());
        else
            return ConditionEvaluationResult.disabled("do not run - only for persistence listener context enabled");
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        BTExecutionContext.INSTANCE.getTestContext().clearResources();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        BTExecutionContext.INSTANCE.getTestContext().clearResources();
    }

}
