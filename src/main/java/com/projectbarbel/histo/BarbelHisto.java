package com.projectbarbel.histo;

import java.time.LocalDate;
import java.util.List;

import com.googlecode.cqengine.query.Query;

public interface BarbelHisto<T> {
    
    void save(T currentVersion, LocalDate from, LocalDate until);

    List<T> retrieve(Query<T> query);

}
