package org.projectbarbel.histo.persistence;

import java.util.Collection;

public interface Read<R,T> {
    Collection<T> execute(R ressource);
}
