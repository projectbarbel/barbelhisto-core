package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class SimpleGsonPojoSerializerTest {

    private SimpleGsonPojoSerializer serializer;
    
    @Test
    public void testSerialize_DefaultDocument() throws Exception {
        serializer = new SimpleGsonPojoSerializer(BarbelHistoBuilder.barbel());
        DefaultDocument initial = BarbelTestHelper.random(DefaultDocument.class);
        byte[] bytes = serializer.serialize(initial);
        DefaultDocument roundtrip = (DefaultDocument)serializer.deserialize(bytes);
        assertEquals(initial, roundtrip);
        assertNotSame(initial, roundtrip);
    }

    @Test
    public void testSerialize_DefaultPojo() throws Exception {
        serializer = new SimpleGsonPojoSerializer(BarbelHistoBuilder.barbel());
        DefaultPojo initial = EnhancedRandom.random(DefaultPojo.class);
        Bitemporal bitemporal = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), initial, BitemporalStamp.createActive());
        byte[] bytes = serializer.serialize(bitemporal);
        DefaultPojo roundtrip = (DefaultPojo)serializer.deserialize(bytes);
        assertEquals(bitemporal, roundtrip);
        assertNotSame(initial, roundtrip);
    }
    
    @Test
    public void testSerialize_SomePojo() throws Exception {
        serializer = new SimpleGsonPojoSerializer(BarbelHistoBuilder.barbel());
        SomePojo initial = EnhancedRandom.random(SomePojo.class);
        Bitemporal bitemporal = BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), initial, BitemporalStamp.createActive());
        byte[] bytes = serializer.serialize(bitemporal);
        SomePojo roundtrip = (SomePojo)serializer.deserialize(bytes);
        assertEquals(bitemporal, roundtrip);
        assertNotSame(initial, roundtrip);
    }
    
    @Test
    public void testSerialize_BitemporalVersion() throws Exception {
        serializer = new SimpleGsonPojoSerializer(BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL));
        BitemporalVersion initial = new BitemporalVersion(BitemporalStamp.createActive(), EnhancedRandom.random(SomePojo.class));
        byte[] bytes = serializer.serialize(initial);
        Bitemporal roundtrip = serializer.deserialize(bytes);
        assertEquals(initial, roundtrip);
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
