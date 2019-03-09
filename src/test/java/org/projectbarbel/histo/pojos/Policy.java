package org.projectbarbel.histo.pojos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@PersistenceConfig(serializer = BarbelPojoSerializer.class, polymorphic = true)
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Policy other = (Policy) obj;
        return Objects.equals(account, other.account) && Objects.equals(drivers, other.drivers)
                && Objects.equals(insurant, other.insurant) && Objects.equals(keeper, other.keeper)
                && paymentMethod == other.paymentMethod && Objects.equals(policyNumber, other.policyNumber)
                && Objects.equals(renewalDate, other.renewalDate) && Objects.equals(risk, other.risk)
                && tarifmodell == other.tarifmodell && timespan == other.timespan && Objects.equals(usage, other.usage)
                && Objects.equals(vehicle, other.vehicle) && vertragStatus == other.vertragStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, drivers, insurant, keeper, paymentMethod, policyNumber, renewalDate, risk,
                tarifmodell, timespan, usage, vehicle, vertragStatus);
    }

}
