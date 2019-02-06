package com.projectbarbel.histo.journal.functions;

import java.util.function.Function;

import com.google.gson.Gson;
import com.projectbarbel.histo.BarbelHistoContext;

public class GsonPojoCopier<T> implements Function<T, T>{

    private Gson gson = BarbelHistoContext.getDefaultGson();
    
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public T apply(T objectFrom) {
        @SuppressWarnings("unchecked")
        T copy = (T) gson.fromJson(gson.toJson(objectFrom), objectFrom.getClass());
        return copy;
    }

}
