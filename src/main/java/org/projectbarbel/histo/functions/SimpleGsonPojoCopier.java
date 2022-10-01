package org.projectbarbel.histo.functions;

import java.util.function.UnaryOperator;

import org.projectbarbel.histo.BarbelHistoContext;

import com.google.gson.Gson;

public class SimpleGsonPojoCopier implements UnaryOperator<Object> {

    private Gson gson = BarbelHistoContext.getDefaultGson();
    public static final SimpleGsonPojoCopier INSTANCE = new SimpleGsonPojoCopier();

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object apply(Object objectFrom) {
        return gson.fromJson(gson.toJson(objectFrom), objectFrom.getClass());
    }

}
