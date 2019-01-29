package com.projectbarbel.histo.functions.update;

import java.time.LocalDate;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.model.Bitemporal;

public class ValidateEffectiveDate implements BiPredicate<Bitemporal<?>, LocalDate> {

    @Override
    public boolean test(Bitemporal<?> currentVersion, LocalDate newEffectiveFrom) {
        Validate.isTrue(newEffectiveFrom.isBefore(currentVersion.getEffectiveUntil()),
                "effective date must be before current versions effective until");
        Validate.isTrue(newEffectiveFrom.isBefore(LocalDate.MAX),
                "effective date cannot be infinite");
        Validate.inclusiveBetween(currentVersion.getEffectiveFrom().toEpochDay(),
                currentVersion.getEffectiveUntil().toEpochDay(),
                newEffectiveFrom.toEpochDay(),
                "effective date of new version must be withing effective period of current version");
        return true;
    }

}
