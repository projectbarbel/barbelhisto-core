package org.projectbarbel.histo.suite.extensions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OnlyEndsWithTest implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestClass().get().getName().endsWith("Test"))
            return ConditionEvaluationResult.enabled("only test classes whose name ends with 'Test' allowed");
        return ConditionEvaluationResult.disabled("run cause ends with 'Test'");
    }
    
}
