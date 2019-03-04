package org.projectbarbel.histo.extension;

import java.util.ArrayList;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.suite.BTSuiteExecutor;
import org.projectbarbel.histo.suite.context.BTTestContextPersistenceListener;

public class BarbelHistoCore_CollectionTestSuite {

    @Test
    void testSuite() throws Exception {
        BTSuiteExecutor executor = new BTSuiteExecutor();
        executor.test(new CollectionContext());
    }

    public static class CollectionContext implements BTTestContextPersistenceListener {

        @Override
        public Function<Class<?>, BarbelHistoBuilder> contextFunction() {
            return new Function<Class<?>, BarbelHistoBuilder>() {

                @Override
                public BarbelHistoBuilder apply(Class<?> t) {
                    DefaultLazyLoadingListener loader = new DefaultLazyLoadingListener(t,
                            BarbelHistoContext.getDefaultGson(), false);
                    DefaultUpdateListener updater = new DefaultUpdateListener(t,
                            BarbelHistoContext.getDefaultGson());
                    return BarbelHistoBuilder.barbel().withSynchronousEventListener(loader).withSynchronousEventListener(updater);
                }
            };
        }

        @Override
        public void clearResources() {
            DefaultLazyLoadingListener.shadow = new ArrayList<>();
        }

    }
}
