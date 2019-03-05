package org.projectbarbel.histo.suite.context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.pojos.Adress;
import org.projectbarbel.histo.pojos.BankAccount;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.Driver;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.Partner;
import org.projectbarbel.histo.pojos.Policy;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.RegisteredKeeper;
import org.projectbarbel.histo.pojos.Risk;
import org.projectbarbel.histo.pojos.Vehicle;
import org.projectbarbel.histo.pojos.VehicleUsage;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;

public class BTTestContextCQEngine implements BTTestContext {

    @SuppressWarnings("rawtypes")
    static final public List<SimpleAttribute> attributes = Arrays.asList(new SimpleAttribute<DefaultPojo, String>("versionId") {
        public String getValue(DefaultPojo object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<PrimitivePrivatePojo, String>("versionId") {
        public String getValue(PrimitivePrivatePojo object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructor, String>("versionId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<PrimitivePrivatePojoPartialContructor, String>("versionId") {
        public String getValue(PrimitivePrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<NoPrimitivePrivatePojoPartialContructor, String>("versionId") {
        public String getValue(NoPrimitivePrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructorWithComplexType, String>("versionId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructorWithComplexType object,
                QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructor, String>("versionId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<Adress, String>("versionId") {
        public String getValue(Adress object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<Driver, String>("versionId") {
        public String getValue(Driver object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<Vehicle, String>("versionId") {
        public String getValue(Vehicle object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<RegisteredKeeper, String>("versionId") {
        public String getValue(RegisteredKeeper object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<Risk, String>("versionId") {
        public String getValue(Risk object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<Policy, String>("versionId") {
        public String getValue(Policy object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<Partner, String>("versionId") {
        public String getValue(Partner object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<VehicleUsage, String>("versionId") {
        public String getValue(VehicleUsage object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<BankAccount, String>("versionId") {
        public String getValue(BankAccount object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<DefaultDocument, String>("versionId") {
        public String getValue(DefaultDocument object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    }, new SimpleAttribute<BitemporalVersion, String>("versionId") {
        public String getValue(BitemporalVersion object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    });

    @Override
    public Function<Class<?>, BarbelHistoBuilder> contextFunction() {
        return new Function<Class<?>, BarbelHistoBuilder>() {

            @SuppressWarnings("unchecked")
            @Override
            public BarbelHistoBuilder apply(Class<?> managedType) {
                return BarbelHistoBuilder.barbel().withBackboneSupplier(() -> new ConcurrentIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(getAttribute(managedType), new File("test.dat"))));
            }

        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <O> SimpleAttribute getAttribute(Class<O> managedType) {
        for (SimpleAttribute<O, ?> simpleAttribute : attributes) {
            if (simpleAttribute.getObjectType().equals(managedType))
                return simpleAttribute;
        }
        throw new IllegalStateException(
                "undefined pojo for test suite with cqengine persistence config: " + managedType.getName());
    }
    
    @Override
    public void clearResources() {
        try {
            Files.deleteIfExists(Paths.get("test.dat"));
            Files.deleteIfExists(Paths.get("test.dat-shm"));
            Files.deleteIfExists(Paths.get("test.dat-wal"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
