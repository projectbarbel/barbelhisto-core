package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.functions.TableJournalPrettyPrinter;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

public class BarbelHistoCore_StdPojoUsage_Test {

    @BeforeAll
    public static void setup() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testSaveTwoAndViewJournal() {
        
        BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
        Employee employee = new Employee("somePersonelNumber", "Niklas", "Schlimm");
        core.save(employee, LocalDate.now(), LocalDate.MAX);

        Optional<Employee> effectiveIn10DaysOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));
        Optional<Employee> effectiveYesterdayOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().minusDays(1)));
        assertFalse(effectiveYesterdayOptional.isPresent());
        
        System.out.println(core.prettyPrintJournal(employee.getId()));

        Optional<Employee> effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
        Employee effectiveEmployeeVersion = effectiveNowOptional.get();
        effectiveEmployeeVersion.setLastname("changedLastName");
        core.save(effectiveEmployeeVersion, LocalDate.now().plusDays(10), LocalDate.MAX);

        effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));

        BitemporalStamp versionData = ((Bitemporal)effectiveNowOptional.get()).getBitemporalStamp();
        assertNotNull(versionData);
        
        effectiveIn10DaysOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));

        effectiveEmployeeVersion = effectiveNowOptional.get();
        Employee effectiveIn10DaysVersion = effectiveIn10DaysOptional.get();
        
        assertTrue(effectiveEmployeeVersion.getLastname().equals("Schlimm"));
        assertTrue(effectiveIn10DaysVersion.getLastname().equals("changedLastName"));

        System.out.println(core.prettyPrintJournal(employee.getId()));

    }
    
    @Test
    public void timeshift() {
        
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 2, 1, 0, 0));
        
        BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
        Employee employee = new Employee("somePersonelNumber", "Niklas", "Schlimm");
        core.save(employee, LocalDate.now(), LocalDate.MAX);
        
        Optional<Employee> effectiveIn10DaysOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));
        Optional<Employee> effectiveYesterdayOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().minusDays(1)));
        assertFalse(effectiveYesterdayOptional.isPresent());
        
        System.out.println(core.prettyPrintJournal(employee.getId()));
        
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.now());

        Optional<Employee> effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
        Employee effectiveEmployeeVersion = effectiveNowOptional.get();
        effectiveEmployeeVersion.setLastname("changedLastName");
        core.save(effectiveEmployeeVersion, LocalDate.now().plusDays(10), LocalDate.MAX);
        
        effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
        effectiveIn10DaysOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));
        
        effectiveEmployeeVersion = effectiveNowOptional.get();
        Employee effectiveIn10DaysVersion = effectiveIn10DaysOptional.get();
        
        assertTrue(effectiveEmployeeVersion.getLastname().equals("Schlimm"));
        assertTrue(effectiveIn10DaysVersion.getLastname().equals("changedLastName"));
        
        System.out.println(core.prettyPrintJournal(employee.getId()));
        
        DocumentJournal journal = core.timeshift("somePersonelNumber", LocalDate.now().minusDays(1).atStartOfDay());
        System.out.println(new TableJournalPrettyPrinter().apply(journal.list()));
        
    }
    
    public static class Employee {
        @DocumentId
        private String personnelNumber; 
        private String firstname; 
        private String lastname;
        private List<Adress> adresses = new ArrayList<>();
        public Employee(String id, String firstname, String lastname) {
            this.personnelNumber = id;
            this.firstname = firstname;
            this.lastname = lastname;
        }
        public String getId() {
            return personnelNumber;
        }
        public String getFirstname() {
            return firstname;
        }
        public String getLastname() {
            return lastname;
        }
        public List<Adress> getAdresses() {
            return adresses;
        }
        public void setId(String id) {
            this.personnelNumber = id;
        }
        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }
        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
        public void setAdresses(List<Adress> adresses) {
            this.adresses = adresses;
        }
    }
    
    public class Adress {
        private String street;
        private String housenumber;
        private String postcode;
        private String city;
        private String country;
        public String getStreet() {
            return street;
        }
        public String getHousenumber() {
            return housenumber;
        }
        public String getPostcode() {
            return postcode;
        }
        public String getCity() {
            return city;
        }
        public String getCountry() {
            return country;
        }
        public void setStreet(String street) {
            this.street = street;
        }
        public void setHousenumber(String housenumber) {
            this.housenumber = housenumber;
        }
        public void setPostcode(String postcode) {
            this.postcode = postcode;
        }
        public void setCity(String city) {
            this.city = city;
        }
        public void setCountry(String country) {
            this.country = country;
        }
    }

}
