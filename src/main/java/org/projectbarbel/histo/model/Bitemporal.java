package org.projectbarbel.histo.model;

public interface Bitemporal {

    BitemporalStamp getBitemporalStamp();

    void setBitemporalStamp(BitemporalStamp stamp);

}
