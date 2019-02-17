package org.projectbarbel.histo.functions;

import java.util.function.Function;

import com.rits.cloning.Cloner;

public class RitsClonerCopyFunction implements Function<Object, Object> {

    public final static RitsClonerCopyFunction INSTANCE = new RitsClonerCopyFunction();
    private Cloner cloner = new Cloner();

    @Override
    public Object apply(Object candidate) {
        return cloner.deepClone(candidate);
    }

}
