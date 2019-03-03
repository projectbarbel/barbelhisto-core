package org.projectbarbel.histo.suite.context;

import java.util.function.Function;

import org.projectbarbel.histo.BarbelHistoBuilder;

public interface BTTestContext {
    Function<Class<?>, BarbelHistoBuilder> contextFunction();
    void clearResources();
}
