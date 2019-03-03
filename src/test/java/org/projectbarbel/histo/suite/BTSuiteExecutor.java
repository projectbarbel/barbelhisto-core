package org.projectbarbel.histo.suite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;

import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.projectbarbel.histo.suite.context.BTTestContext;

public class BTSuiteExecutor {

    private SummaryGeneratingListener listener = new SummaryGeneratingListener();
    private int testcount = 0;
    private ClassNameFilter filter = ClassNameFilter.includeClassNamePatterns(".*SuiteTest");

    public void runNeutral() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage("org.projectbarbel.histo"))
                .filters(filter,
                        ClassNameFilter.excludeClassNamePatterns(".*StandardSuiteTest"))
                .build();

        Launcher launcher = LauncherFactory.create();

        // Register a listener of your choice
        launcher.registerTestExecutionListeners(listener, new TestExecutionListener() {
            public void executionStarted(TestIdentifier testIdentifier) {
                testcount++;
                System.out.println("Starting test suite case [" + testcount + "]: " + testIdentifier.getDisplayName());
            }
        });

        launcher.execute(request);
    }

    public void test(BTTestContext context) {
        BTTestContext previousContext = BTExecutionContext.INSTANCE.getTestContext();
        BTExecutionContext.INSTANCE.setTestContext(context);
        runNeutral();
        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));
        summary.printFailuresTo(new PrintWriter(System.out));
        assertEquals(0, summary.getFailures().size());
        BTExecutionContext.INSTANCE.setTestContext(previousContext);
    }

    public void test(BTTestContext context, Class<?> testClass) {
        filter = ClassNameFilter.includeClassNamePatterns(testClass.getName());
        test(context);
    }
    
}
