package com.projectbarbel.histo.model;

import java.util.function.Function;

public class KeepSubsequentUpdatePolicy implements Function<Journal<Bitemporal<?>, ?>, Journal<Bitemporal<?>, ?>> {

    @Override
    public Journal<Bitemporal<?>, ?> apply(Journal<Bitemporal<?>, ?> t) {
        return t;
    }

}
