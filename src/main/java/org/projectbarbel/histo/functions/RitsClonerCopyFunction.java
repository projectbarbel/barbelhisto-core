package org.projectbarbel.histo.functions;

import java.util.function.UnaryOperator;

import com.rits.cloning.Cloner;

public class RitsClonerCopyFunction implements UnaryOperator<Object> {

    public static final RitsClonerCopyFunction INSTANCE = new RitsClonerCopyFunction();
    private Cloner cloner = new Cloner();

    @Override
    public Object apply(Object candidate) {
        return cloner.deepClone(candidate);
    }

}
