package org.projectbarbel.histo.suite.extensions;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.context.BTContext_CQEngine;

public class BTC_CQPersistence implements BeforeAllCallback, AfterAllCallback, ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (BTExecutionContext.INSTANCE.getTestContext() instanceof BTContext_CQEngine)
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
