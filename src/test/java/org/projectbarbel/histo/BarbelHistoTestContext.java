package org.projectbarbel.histo;

import java.util.function.Function;

public class BarbelHistoTestContext {
    public static Function<Class<?>, BarbelHistoBuilder> INSTANCE = (t) -> BarbelHistoBuilder.barbel();
}
