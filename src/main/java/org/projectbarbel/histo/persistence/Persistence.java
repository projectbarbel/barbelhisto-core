package org.projectbarbel.histo.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.projectbarbel.histo.BarbelHisto;

public interface Persistence<C,R,T> {

    C getConnection(Map<Object, Object> options);
    R openRequestScopeResource(Map<Object, Object> options);
    void closeRequestScopeResource(Map<Object, Object> options);
    void closeConnection(C connection);
    int delete(Function<BarbelHisto<T>, List<T>> objectquery, BiConsumer<R, List<T>> deleteFunction);
    int update(Function<BarbelHisto<T>, List<T>> objectquery, BiConsumer<R, List<T>> updateFunction);
    int create(Function<BarbelHisto<T>, List<T>> objectquery, BiConsumer<R, List<T>> insertFunction);
    Collection<T> read(Function<R, List<T>> objectquery);

}
