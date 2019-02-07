package com.projectbarbel.histo;

import java.time.LocalDate;
import java.util.List;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;

public interface BarbelHisto {
    
    boolean save(Object currentVersion, LocalDate from, LocalDate until);

    <T> List<T> retrieve(Query<T> query);

    <T> List<T> retrieve(Query<T> query, QueryOptions options);

    String prettyPrintJournal(Object id);

}
