package org.projectbarbel.histo.performance;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelHistoTestContext;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelCoreSaveMemory_SuiteTest {

    private final BarbelHisto<DefaultPojo> core = BarbelHistoTestContext.INSTANCE.apply(DefaultPojo.class).build();

    static ScheduledThreadPoolExecutor executor; 
    static ScheduledFuture<?> t;
    private int pojoCount = 50;
    private int maxVersions = 5000;
    private boolean dump = false;

    private long timeoutInSeconds = 10;    

    static class MyTask implements Runnable {
		private BarbelHisto<DefaultPojo> core;
		private boolean dump;
		private int pojoCount;
        public MyTask(BarbelHisto<DefaultPojo> core, boolean dump, int maxVersions, int pojoCount) {
			this.core = core;
			this.dump = dump;
			this.pojoCount = pojoCount;
        	
		}
        @Override
        public void run() {
            List<DefaultPojo> pojos = EnhancedRandom.randomListOf(pojoCount, DefaultPojo.class);
            String id = EnhancedRandom.random(String.class);
            for (DefaultPojo pojo : pojos) {
                pojo.setDocumentId(id);
            }
            long time = new Date().getTime();
            for (Object pojo : pojos) {
                core.save((DefaultPojo) pojo,
                        BarbelTestHelper.randomLocalDate(LocalDate.of(2010, 1, 1), LocalDate.of(2015, 1, 1)),
                        BarbelTestHelper.randomLocalDate(LocalDate.of(2015, 1, 2), LocalDate.of(2020, 1, 1)));
            }
            System.out.println("######### Barbel-Statistics #########");
            BigDecimal timetaken = new BigDecimal((new Date().getTime() - time)).divide(new BigDecimal(1000))
                    .round(new MathContext(4, RoundingMode.HALF_UP));
            System.out.println("inserterted " + pojoCount + " in " + timetaken + " s");
            System.out.println("per object: " + new BigDecimal(new Date().getTime() - time).divide(new BigDecimal(pojoCount))
            .round(new MathContext(4, RoundingMode.HALF_UP)) + " ms");
            printBarbelStatitics();
            if (dump)
                ((BarbelHistoCore<DefaultPojo>)core).unloadAll();
            printMemory();
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
        t = executor.scheduleAtFixedRate(new MyTask(core, dump, maxVersions, pojoCount), 0, 2, TimeUnit.SECONDS);
        assertThrows(TimeoutException.class, ()->t.get(timeoutInSeconds ,TimeUnit.SECONDS));
        executor.shutdownNow();
    }
    
}