package org.projectbarbel.histo.suite.extensions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.context.BTTestContextCQEngine;
import org.projectbarbel.histo.suite.context.BTTestContextPersistenceListener;

public class BTPersistenceOnly implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if ((BTExecutionContext.INSTANCE.getTestContext() instanceof BTTestContextCQEngine)
                || (BTExecutionContext.INSTANCE.getTestContext() instanceof BTTestContextPersistenceListener))
            return ConditionEvaluationResult.enabled(
                    "runs for listener context: " + BTExecutionContext.INSTANCE.getTestContext().getClass().getName());
        else
            return ConditionEvaluationResult.disabled("do not run - only for persistence listener context enabled");
    }

}
