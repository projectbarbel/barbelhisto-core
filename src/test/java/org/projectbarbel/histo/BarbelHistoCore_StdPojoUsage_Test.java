package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.EffectivePeriod;

public class BarbelHistoCore_StdPojoUsage_Test {

    @BeforeAll
    public static void setup() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testSaveTwoAndViewJournal() {

    	// save one
        BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
        Employee employee = new Employee("somePersonelNumber", "Martin", "Smith");
        core.save(employee);

        // get the effective version today
        Employee effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));

        // get the effective version in 10 days
        Employee effectiveIn10Days = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().plusDays(10)));

        // yesterday nothing was effective, this will throw an exception stating no value present
        assertThrows(NoSuchElementException.class, ()-> core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().minusDays(1))));

        // access the version data
        BitemporalStamp versionData = ((Bitemporal)effectiveEmployeeVersion).getBitemporalStamp();
        assertNotNull(versionData);

        // print a nice journal output
        System.out.println(core.prettyPrintJournal(employee.getId()));

        // make a change cause employee marries in 10 days
        effectiveEmployeeVersion.setLastname("changedLastName");
        core.save(effectiveEmployeeVersion, BarbelHistoContext.getBarbelClock().now().plusDays(10), EffectivePeriod.INFINITE);

        // retrieve the two versions
        effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
        effectiveIn10Days = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().plusDays(10)));

        assertTrue(effectiveEmployeeVersion.getLastname().equals("Smith"));
        assertTrue(effectiveIn10Days.getLastname().equals("changedLastName"));

        System.out.println(core.prettyPrintJournal(employee.getId()));

    }

    @Test
    public void loadUnload() {
        BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
        Employee employee = new Employee("somePersonelNumber", "Niklas", "Schlimm");
        Employee effectiveEmployeeVersion = (Employee)core.save(employee).getUpdateRequest();
        effectiveEmployeeVersion.setLastname("changedLastName");
        core.save(effectiveEmployeeVersion, BarbelHistoContext.getBarbelClock().now().plusDays(10));
        Collection<Bitemporal> unloadVersionData = core.unload("somePersonelNumber");
        assertEquals(3, unloadVersionData.size());
        core.load(unloadVersionData);
	}

    @Test
    public void timeshift() {

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 2, 1, 0, 0).atZone(ZoneId.systemDefault()));

        BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
        Employee employee = new Employee("somePersonelNumber", "Niklas", "Schlimm");
        core.save(employee);

        Employee effectiveIn10Days = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().plusDays(10)));
        assertThrows(NoSuchElementException.class, ()->core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().minusDays(1))));

        System.out.println(core.prettyPrintJournal(employee.getId()));

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.now().atZone(ZoneId.systemDefault()));

        Employee effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
        effectiveEmployeeVersion.setLastname("changedLastName");
        core.save(effectiveEmployeeVersion);

        effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
        effectiveIn10Days = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().plusDays(10)));
        Employee effectiveYesterday = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, BarbelHistoContext.getBarbelClock().now().minusDays(1)));

        assertTrue(effectiveEmployeeVersion.getLastname().equals("changedLastName"));
        assertTrue(effectiveIn10Days.getLastname().equals("changedLastName"));
        assertEquals("Schlimm", effectiveYesterday.getLastname());

        System.out.println(core.prettyPrintJournal(employee.getId()));

        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();

        DocumentJournal journal = core.timeshift("somePersonelNumber", BarbelHistoContext.getBarbelClock().now().minusDays(1).withHour(0).withMinute(0).withSecond(0));

        assertTrue(journal.size()==1);
        assertEquals(((Employee)journal.list().get(0)).getLastname(), "Schlimm");

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
