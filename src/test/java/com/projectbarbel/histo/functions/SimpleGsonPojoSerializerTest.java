package com.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.projectbarbel.histo.BarbelHistoBuilder;
import com.projectbarbel.histo.BarbelMode;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.functions.SimpleGsonPojoSerializer;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class SimpleGsonPojoSerializerTest {

    private SimpleGsonPojoSerializer serializer = new SimpleGsonPojoSerializer();
    
    @Test
    public void testSerialize_DefaultDocument() throws Exception {
        DefaultDocument initial = BarbelTestHelper.random(DefaultDocument.class);
        byte[] bytes = serializer.serialize(initial);
        DefaultDocument roundtrip = (DefaultDocument)serializer.deserialize(bytes);
        assertEquals(initial, roundtrip);
        assertNotSame(initial, roundtrip);
    }

    @Test
    public void testSerialize_DefaultOjo() throws Exception {
        DefaultPojo initial = EnhancedRandom.random(DefaultPojo.class);
        Bitemporal bitemporal = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), initial, BitemporalStamp.createActive());
        byte[] bytes = serializer.serialize(bitemporal);
        DefaultPojo roundtrip = (DefaultPojo)serializer.deserialize(bytes);
        assertEquals(bitemporal, roundtrip);
        assertNotSame(initial, roundtrip);
    }
    
    @Test
    public void testSerialize_SomePojo() throws Exception {
        SomePojo initial = EnhancedRandom.random(SomePojo.class);
        Bitemporal bitemporal = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), initial, BitemporalStamp.createActive());
        byte[] bytes = serializer.serialize(bitemporal);
        SomePojo roundtrip = (SomePojo)serializer.deserialize(bytes);
        assertEquals(bitemporal, roundtrip);
        assertNotSame(initial, roundtrip);
    }
    
    public static class SomePojo {
        private String aString;
        public String getaString() {
            return aString;
        }
        public void setaString(String aString) {
            this.aString = aString;
        }
        private List<String> aStringList;
        public List<String> getaStringList() {
            return aStringList;
        }
        public void setaStringList(List<String> aStringList) {
            this.aStringList = aStringList;
        }
        @Override
        public int hashCode() {
            return Objects.hash(aString, aStringList);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SomePojo)) {
                return false;
            }
            SomePojo other = (SomePojo) obj;
            return Objects.equals(aString, other.aString) && Objects.equals(aStringList, other.aStringList);
        }
    }
    
}
