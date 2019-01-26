package com.projectbarbel.histo.model;

import java.time.LocalDate;

public interface Updater {

    Updater inActivity(String activity);

    Updater createdBy(String user);

    Updater effectiveFrom(LocalDate newEffectiveDate);

    Updater execute();

    Bitemporal<?> precedingVersion();

    Bitemporal<?> subsequentVersion();

}