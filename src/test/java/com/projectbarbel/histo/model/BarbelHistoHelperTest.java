package com.projectbarbel.histo.model;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoHelper;

public class BarbelHistoHelperTest {

    @Test
    public void testEffectiveDateToEffectiveInstant() {
        LocalDate effectiveDate = LocalDate.now();
        Instant effectiveStamp = BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(effectiveDate);
        LocalDate readED = BarbelHistoHelper.effectiveInstantToEffectiveDate(effectiveStamp);
        assertEquals(effectiveDate, readED);
        LocalDate effectDate = BarbelHistoHelper.effectiveInstantToEffectiveDate(effectiveStamp);
        Instant effectInst = BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(effectDate);
        assertEquals(effectiveStamp, effectInst);
    }

    @Test
    public void testEffectiveInstantToEffectiveDate() {
        Instant effectiveUTCInstant = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC); // assuming UTC time in bitemporal stamp
        LocalDate effectiveDate = BarbelHistoHelper.effectiveInstantToEffectiveDate(effectiveUTCInstant);
        Instant readInst = BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(effectiveDate);
        System.out.println("Diff:"+(effectiveUTCInstant.getEpochSecond()-readInst.getEpochSecond()));
        assertEquals(effectiveUTCInstant, readInst);
    }

    @Test
    public void testTransactionTimeToTransactionInstant() {
        ZonedDateTime time = ZonedDateTime.now();
        Instant utcInstant = BarbelHistoHelper.transactionTimeToTransactionInstant(time);
        ZonedDateTime readTime = BarbelHistoHelper.transactionInstantToTransactionTime(utcInstant, ZoneId.systemDefault());
        assertEquals(time, readTime);
    }

    @Test
    public void testTransactionInstantToTransactionTime() {
        Instant transactionUTCIstant = Clock.systemUTC().instant(); // assuming UTC instant in bitemporal stamp
        ZonedDateTime tarnsactionTime = BarbelHistoHelper.transactionInstantToTransactionTime(transactionUTCIstant, ZoneId.systemDefault());
        Instant transactionInstantRead = BarbelHistoHelper.transactionTimeToTransactionInstant(tarnsactionTime);
        assertEquals(transactionUTCIstant, transactionInstantRead);
    }

}
