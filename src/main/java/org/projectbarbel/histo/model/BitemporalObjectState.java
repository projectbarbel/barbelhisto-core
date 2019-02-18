package org.projectbarbel.histo.model;

/**
 * Versions have two main states: active means they are valid from a business
 * viewpoint. Inactive means "deleted" from a business viewpoint.
 * 
 * @author Niklas Schlimm
 *
 */
public enum BitemporalObjectState {
    ACTIVE, INACTIVE;
}
