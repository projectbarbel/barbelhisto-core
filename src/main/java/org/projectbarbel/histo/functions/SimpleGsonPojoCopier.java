package org.projectbarbel.histo.functions;

import java.util.function.Function;

import org.projectbarbel.histo.BarbelHistoContext;

import com.google.gson.Gson;

public class SimpleGsonPojoCopier implements Function<Object, Object> {

    private Gson gson = BarbelHistoContext.getDefaultGson();

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object apply(Object objectFrom) {
        Object copy = gson.fromJson(gson.toJson(objectFrom), objectFrom.getClass());
        return copy;
    }

}
