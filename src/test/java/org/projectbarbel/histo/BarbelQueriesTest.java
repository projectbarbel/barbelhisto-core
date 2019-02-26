package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.EffectivePeriod;

public class BarbelQueriesTest {

    @Test
    public void testReturnIDForQuery() throws Exception {
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.effectiveNow("some")));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.all("some")));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.allActive("some")));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.allInactive("some")));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.effectiveAfter("some", LocalDate.now())));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.effectiveBetween("some", EffectivePeriod.nowToInfinite())));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.effectiveAt("some", LocalDate.now())));
        assertEquals("some",BarbelQueries.returnIDForQuery(BarbelQueries.journalAt("some", LocalDateTime.now())));
    }

}