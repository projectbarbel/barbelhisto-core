package org.projectbarbel.histo.model;

import org.projectbarbel.histo.AbstractBarbelMode;

/**
 * The target proxy interface applied to POJOs when running in
 * {@link AbstractBarbelMode#POJO}.
 * 
 * @author Niklas Schlimm
 *
 */
public interface BarbelProxy {

    Object getTarget();

    void setTarget(Object target);

}
