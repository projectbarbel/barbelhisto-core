package org.projectbarbel.histo.persistence;

import java.util.Collection;
import java.util.Map;

public interface Persistence<C,R,T> {

    C getConnection(Map<Object, Object> options);
    R openRequestScopeResource(Map<Object, Object> options);
    void closeRequestScopeResource(R ressource, Map<Object, Object> options);
    void closeConnection(C connection);
    int delete(Delete<R,T> statement);
    int update(Update<R,T> statement);
    int create(Create<R,T> statement);
    Collection<T> read(Read<R,T> statement);

}
