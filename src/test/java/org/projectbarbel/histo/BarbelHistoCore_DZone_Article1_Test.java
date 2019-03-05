package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
		Client client = new Client("1234", "Martin", "Smith");
		barbel.save(client, LocalDate.now(), LocalDate.MAX);

		Client effectiveNow = barbel.retrieveOne(BarbelQueries.effectiveNow("1234"));
		effectiveNow.getAdresses().add(new Adress("Barbel Street 10", "Houston"));
		barbel.save(effectiveNow, LocalDate.of(2019, 3, 1), LocalDate.MAX);

		System.out.println(barbel.prettyPrintJournal("1234"));

		assertNotNull(effectiveNow); // dummy assert to avoid warnings in Sonar Cube

	}

	public static class Client {
		@DocumentId
		private String clientID;
		private String firstname;
		private String lastname;
		private List<Adress> adresses = new ArrayList<>();

		public Client(String id, String firstname, String lastname) {
			this.clientID = id;
			this.firstname = firstname;
			this.lastname = lastname;
		}

		public String getId() {
			return clientID;
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
			this.clientID = id;
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
			this.street = street;
			this.city = city;
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
