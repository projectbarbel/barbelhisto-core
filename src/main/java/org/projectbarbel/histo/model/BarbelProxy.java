package org.projectbarbel.histo.model;

import org.projectbarbel.histo.BarbelMode;

/**
 * The target proxy interface applied to POJOs when running in
 * {@link BarbelMode#POJO}.
 * 
 * @author Niklas Schlimm
 *
 */
public interface BarbelProxy {

    Object getTarget();

    void setTarget(Object target);

}
