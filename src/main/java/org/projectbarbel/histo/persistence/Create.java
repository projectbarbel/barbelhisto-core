package org.projectbarbel.histo.persistence;

import java.util.List;
import java.util.function.Function;

import org.projectbarbel.histo.BarbelHisto;

public interface Create<R,T> {
    int execute(R resource, List<T> objects);
    Function<BarbelHisto<T>, List<T>> queryObjects();
}
