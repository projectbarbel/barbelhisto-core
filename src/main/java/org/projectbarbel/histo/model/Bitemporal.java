package org.projectbarbel.histo.model;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelMode;

/**
 * Interface applied to all objects managed by {@link BarbelHisto}. In
 * {@link BarbelMode#BITEMPORAL} clients implement this interface on their
 * business classes. There is nothing else required other then implementing this
 * interface. This will require to add a {@link BitemporalStamp} field to the
 * business class.
 * 
 * @author Niklas Schlimm
 *
 */
public interface Bitemporal {

    BitemporalStamp getBitemporalStamp();
    void setBitemporalStamp(BitemporalStamp stamp);

}
