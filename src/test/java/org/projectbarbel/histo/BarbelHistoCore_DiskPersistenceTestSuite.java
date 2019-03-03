package org.projectbarbel.histo;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.suite.BTSuiteExecutor;
import org.projectbarbel.histo.suite.context.BTTestContextCQEngine;

public class BarbelHistoCore_DiskPersistenceTestSuite {

    @Test
    void testSuite() throws Exception {
        BTSuiteExecutor executor = new BTSuiteExecutor();
        executor.test(new BTTestContextCQEngine());
    }
}
