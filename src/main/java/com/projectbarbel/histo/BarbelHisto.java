package com.projectbarbel.histo;

import java.time.LocalDate;

public interface BarbelHisto {
    
    void save(Object currentVersion, LocalDate from, LocalDate until);

}
