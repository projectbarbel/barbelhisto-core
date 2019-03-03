package org.projectbarbel.histo.pojos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class Policy {

    @DocumentId
    private String policyNumber;
    private Partner insurant;
    private BankAccount account;
    private PaymentMethod paymentMethod;
    private Risk risk;
    private LocalDate renewalDate;
    private int timespan;
    private Product tarifmodell;
    private PolicyState vertragStatus;
    private BigDecimal premium;
    private Vehicle vehicle;
    private VehicleUsage usage;
    private RegisteredKeeper keeper;
    private List<Driver> drivers;

}
