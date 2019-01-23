package com.projectbarbel.histo.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Journal<T extends Bitemporal<O>, O> {

    private Map<O, Bitemporal<O>> journal;

    private Journal(Map<O, Bitemporal<O>> journal) {
        super();
        this.journal = journal;
    }

    public Bitemporal<O> getOne(O versionId) {
        return journal.get(versionId);
    };

    public List<? extends Bitemporal<O>> addOne(Bitemporal<O> bitemporal) {
        journal.put(bitemporal.getVersionId(), bitemporal);
        return list();
    }
    
    public int size() {
        return journal.size();
    }
    
    public List<? extends Bitemporal<O>> list() {
        return journal.values().stream().collect(Collectors.toList());
    }
    
    public static <O> Journal<Bitemporal<O>, O> instanceByList(List<? extends Bitemporal<O>> listOfBitemporal) {
        return new Journal<Bitemporal<O>, O>(
                listOfBitemporal.stream().collect(Collectors.toMap(Bitemporal::getVersionId, Function.identity())));
    }

}
