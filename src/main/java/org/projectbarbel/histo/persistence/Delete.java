package org.projectbarbel.histo.persistence;

import java.util.List;
import java.util.function.Function;

import org.projectbarbel.histo.BarbelHisto;

public interface Delete<R,T> {
    int execute(R ressource, List<T> objects);
    Function<BarbelHisto<T>, List<T>> queryObjects();
}
