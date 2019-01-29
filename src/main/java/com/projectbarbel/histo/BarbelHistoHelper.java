package com.projectbarbel.histo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.projectbarbel.histo.model.BitemporalStamp;

public class BarbelHistoHelper {

    /**
     * Notice that the effective date is just a functional date, we just assume its
     * UTC. Nothings changed. 
     */
    public static Instant effectiveDateToEffectiveUTCInstant(LocalDate effectiveDate) {
        return effectiveDate == null ? null : effectiveDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    /**
     * Notice that the effective date is just a functional date, we just assume its
     * UTC. Nothings changed. 
     */
    public static LocalDate effectiveInstantToEffectiveDate(Instant effectiveInstant) {
        return effectiveInstant == null ? null : effectiveInstant.atZone(ZoneId.of("Z")).toLocalDate();
    }

    /**
     * Notice that we treat transaction time different. We assume that the server
     * runs somewhere in the world and therefore we convert the zoned date time to
     * UTC instant and vice versa to make it generic. This is required due to the
     * semantics of transaction time. 
     */
    public static Instant transactionTimeToTransactionInstant(ZonedDateTime zonedTransactionTime) {
        return zonedTransactionTime == null ? null : zonedTransactionTime.withZoneSameLocal(ZoneId.of("Z")).toInstant();
    }

    /**
     * Notice that we treat transaction time different. We assume that the server
     * runs somewhere in the world and therefore we convert the zoned date time to
     * UTC instant and vice versa to make it generic. This is required due to the
     * semantics of transaction time. 
     */
    public static ZonedDateTime transactionInstantToTransactionTime(Instant transactionUTCInstant, ZoneId targetZone) {
        return transactionUTCInstant.atZone(ZoneId.of("Z")).withZoneSameLocal(targetZone);
    }

}
