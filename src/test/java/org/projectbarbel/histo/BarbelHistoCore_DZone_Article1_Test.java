package org.projectbarbel.histo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BarbelHistoCore_DZone_Article1_Test {

    @BeforeAll
    public static void setup() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testDSoneTest() {
        
        BarbelHisto<Client> barbel = BarbelHistoBuilder.barbel().build();
        Client client = new Client("1234", "Niklas", "Schlimm");
        barbel.save(client, LocalDate.now(), LocalDate.MAX);
        
        Optional<Client> effectiveNowOptional = barbel.retrieveOne(BarbelQueries.effectiveNow("1234"));
        Client client1234 = effectiveNowOptional.get();
        client1234.getAdresses().add(new Adress("Barbel Street 10", "Houston"));
        barbel.save(client1234, LocalDate.of(2019,3,1), LocalDate.MAX);
        
        System.out.println(barbel.prettyPrintJournal("1234"));

    }
        
    public static class Client {
        @DocumentId
        private String clientNumber; 
        private String firstname; 
        private String lastname;
        private List<Adress> adresses = new ArrayList<>();
        public Client(String id, String firstname, String lastname) {
            this.clientNumber = id;
            this.firstname = firstname;
            this.lastname = lastname;
        }
        public String getId() {
            return clientNumber;
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
            this.clientNumber = id;
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

        public Adress(String street, String city) {
            this.street=street;
            this.city=city;
        }

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
