package org.projectbarbel.histo.performance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.BarbelHistoCore.DumpMode;
import org.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelCoreSaveMemoryTest {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final BarbelHisto<DefaultPojo> core = BarbelHistoBuilder.barbel().build();
    private int pojoCount = 5000;
    private int maxVersions = 30000;
    private boolean dump = true;

    public static void main(String[] args) throws InterruptedException {
        BarbelCoreSaveMemoryTest test = new BarbelCoreSaveMemoryTest();
        test.scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                List<DefaultPojo> pojos = EnhancedRandom.randomListOf(test.pojoCount, DefaultPojo.class);
                String id = EnhancedRandom.random(String.class);
                for (DefaultPojo pojo : pojos) {
                    pojo.setDocumentId(id);
                }
                long time = new Date().getTime();
                for (Object pojo : pojos) {
                    test.core.save((DefaultPojo) pojo,
                            BarbelTestHelper.randomLocalDate(LocalDate.of(2010, 1, 1), LocalDate.of(2015, 1, 1)),
                            BarbelTestHelper.randomLocalDate(LocalDate.of(2015, 1, 2), LocalDate.of(2020, 1, 1)));
                }
                System.out.println("######### Barbel-Statistics #########");
                BigDecimal timetaken = new BigDecimal((new Date().getTime() - time)).divide(new BigDecimal(1000))
                        .round(new MathContext(4, RoundingMode.HALF_UP));
                System.out.println("inserterted " + test.pojoCount + " in " + timetaken + " s");
                System.out.println("per object: " + new BigDecimal(new Date().getTime() - time).divide(new BigDecimal(test.pojoCount))
                .round(new MathContext(4, RoundingMode.HALF_UP)) + " ms");
                printBarbelStatitics();
                if (test.dump)
                    test.core.dump(DumpMode.CLEARCOLLECTION);
                printMemory();
            }

            @SuppressWarnings("rawtypes")
            private void printBarbelStatitics() {
                int size = ((BarbelHistoCore) test.core).size();
                System.out.println("count of versions: " + size);
                if (size>test.maxVersions)
                    test.scheduler.shutdownNow();
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
        }, 2, 10, TimeUnit.SECONDS);
    }
}