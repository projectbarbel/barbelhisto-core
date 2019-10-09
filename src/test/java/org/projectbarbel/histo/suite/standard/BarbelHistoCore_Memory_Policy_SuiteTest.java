package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.pojos.Policy;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

import io.github.benas.randombeans.api.EnhancedRandom;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_Memory_Policy_SuiteTest {

    private final BarbelHisto<Policy> core = BTExecutionContext.INSTANCE.barbel(Policy.class).build();

    static ScheduledThreadPoolExecutor executor; // no
    static ScheduledFuture<?> t;
    private int pojoCount = 50;
    private int maxVersions = 500000;
    private boolean dump = false;
    private int cycles = 1;
    private int timoutminutes = 5;

    static class MyTask implements Runnable {
		private BarbelHisto<Policy> core;
		private boolean dump;
		private int pojoCount;
        private int cycles;
        private int counter = 0;
        public MyTask(BarbelHisto<Policy> core, boolean dump, int maxVersions, int pojoCount, int cycles) {
			this.core = core;
			this.dump = dump;
			this.pojoCount = pojoCount;
            this.cycles = cycles;
        	
		}
        @Override
        public void run() {
            counter++;
            List<Policy> pojos = EnhancedRandom.randomListOf(pojoCount, Policy.class);
            String id = EnhancedRandom.random(String.class);
            for (Policy pojo : pojos) {
                pojo.setPolicyNumber(id);
            }
            long time = new Date().getTime();
            for (Object pojo : pojos) {
                core.save((Policy) pojo,
                        BarbelTestHelper.randomLocalTime(ZonedDateTime.parse("2010-01-01T00:00:00Z"), ZonedDateTime.parse("2015-01-01T00:00:00Z")),
                        BarbelTestHelper.randomLocalTime(ZonedDateTime.parse("2015-01-02T00:00:00Z"), ZonedDateTime.parse("2020-01-01T00:00:00Z")));
            }
            System.out.println("######### Barbel-Statistics #########");
            BigDecimal timetaken = new BigDecimal((new Date().getTime() - time)).divide(new BigDecimal(1000))
                    .round(new MathContext(4, RoundingMode.HALF_UP));
            System.out.println("inserterted " + pojoCount + " in " + timetaken + " s");
            System.out.println("per object: " + new BigDecimal(new Date().getTime() - time).divide(new BigDecimal(pojoCount))
            .round(new MathContext(4, RoundingMode.HALF_UP)) + " ms");
            printBarbelStatitics();
            if (dump)
                ((BarbelHistoCore<Policy>)core).unloadAll();
            printMemory();
            if (counter==cycles) {
                executor.shutdown();
                throw new IllegalStateException("interrupted");
            }
        }

        @SuppressWarnings("rawtypes")
        private void printBarbelStatitics() {
            int size = ((BarbelHistoCore) core).size();
            System.out.println("count of versions: " + size);
        }

        private void printMemory() {
            int mb = 1024 * 1024;
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            System.out.println("##### Heap utilization statistics [MB] #####");
            System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
            System.out.println("Free Memory:" + runtime.freeMemory() / mb);
            System.out.println("Total Memory:" + runtime.totalMemory() / mb);
            System.out.println("Max Memory:" + runtime.maxMemory() / mb);
        }
    }

    @Test
    public void testMemory() throws InterruptedException, ExecutionException, TimeoutException {
        executor = new ScheduledThreadPoolExecutor(1);
        t = executor.scheduleAtFixedRate(new MyTask(core, dump, maxVersions, pojoCount, cycles), 0, 2, TimeUnit.SECONDS);
        executor.awaitTermination(timoutminutes, TimeUnit.MINUTES);
        assertTrue(executor.isTerminated());
    }
    
}
