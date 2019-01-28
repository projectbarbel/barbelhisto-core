package com.projectbarbel.histo.persistence.impl.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.persistence.api.DocumentDao;

public class MongoDocumentDaoImpl_Create_IntegrationTest {

    private static DocumentDao<DefaultMongoValueObject, ObjectId> dao;
    private MongoCollection<DefaultMongoValueObject> col;

    @BeforeClass
    public static void beforeClass() {
        BarbelHistoOptions opts = BarbelHistoOptions.withDaoClassName("com.projectbarbel.histo.persistence.impl.mongo.MongoDocumentDaoImpl");
        BarbelHistoContext ctx = BarbelHistoContext.of(opts);
        dao = ctx.factory().instanceOf(DefaultHistoType.DAO, new Object[] {FlapDoodleEmbeddedMongoClientDaoSupplier.MONGOCLIENT.getMongo(), "test", "testCol"});
    }

    @Before
    public void setUp() {
        dao.reset();
        col = ((MongoDocumentDaoImpl)dao).getCol();
    }

    @Test
    public void testCreateDocument_and_reset() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        dao.createDocument(object);
        DefaultMongoValueObject doc = dao.readDocument(object.getVersionId()).get();
        assertNotNull(doc);
        dao.reset();
        doc = dao.readDocument(object.getVersionId()).orElse(null);
        assertNull(doc);
    }

    @Test(expected=MongoWriteException.class)
    public void testCreateDocument_and_CreateAgain_shouldThrowexception() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        dao.createDocument(object);
        DefaultMongoValueObject doc = dao.readDocument(object.getVersionId()).get();
        assertNotNull(doc);
        dao.createDocument(object);
    }
    
    @Test
    public void testCreateDocument_findOne_byEffectiveFrom() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        dao.createDocument(object);
        DefaultMongoValueObject readObj = col.find(Filters.eq("bitemporalStamp.effectiveFrom.seconds",
                object.getBitemporalStamp().getEffectiveTime().from.getEpochSecond())).first();
        assertEquals(object, readObj);
    }

    @Test
    public void testCreateDocument_findNone_byEffectiveFrom() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        dao.createDocument(object);
        // try find one where stored effective date is equal to specified value, when
        // specified value is non equal
        DefaultMongoValueObject doc = col.find(Filters.eq("bitemporalStamp.effectiveFrom.seconds", 010101L)).first();
        assertNull(doc);
    }

    @Test
    public void testCreateDocument_findOne_byEffectiveFrom_GreaterEquals_EffectiveDate_Equal() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        long epocheseconds = object.getBitemporalStamp().getEffectiveTime().from.getEpochSecond();
        dao.createDocument(object);
        // try find one where stored effective date is greater/equals to specified
        // value, when specified value is equal
        DefaultMongoValueObject doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds))
                .first();
        assertNotNull(doc);
    }

    @Test
    public void testCreateDocument_findNone_byEffectiveFrom_GreaterEquals_EffectiveDate_Greater() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        long epocheseconds = object.getBitemporalStamp().getEffectiveTime().from.getEpochSecond() + 2L;
        dao.createDocument(object);
        // try find one where stored effective date is greater/equals to specified
        // value, when specified value is higher
        DefaultMongoValueObject doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds))
                .first();
        assertNull(doc);
    }

    @Test
    public void testCreateDocument_findOne_byEffectiveFrom_GreaterEquals_EffectiveDate_Lower() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        long epocheseconds = object.getBitemporalStamp().getEffectiveTime().from.getEpochSecond() - 2L;
        dao.createDocument(object);
        // try find one where stored effective date is greater/equals to specified
        // value, when specified value is lower
        DefaultMongoValueObject doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds))
                .first();
        assertNotNull(doc);
    }

    @Test
    public void testCreateDocument_findOne_byEffectiveFrom_LowerEquals_EffectiveDate_Greater() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        long epocheseconds = object.getBitemporalStamp().getEffectiveTime().from.getEpochSecond() + 2L;
        dao.createDocument(object);
        // try find one where stored effective date is lower/equals to specified value,
        // when specified value is higher
        DefaultMongoValueObject doc = col.find(Filters.lte("bitemporalStamp.effectiveFrom.seconds", epocheseconds))
                .first();
        assertNotNull(doc);
    }

    @Test
    public void testCreateDocument_findOne_byEffectiveFrom_LowerEquals_EffectiveDate_Equals() {
        DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
        long epocheseconds = object.getBitemporalStamp().getEffectiveTime().from.getEpochSecond();
        dao.createDocument(object);
        // try find one where stored effective date is lower/equals to specified value,
        // when specified value is equal
        DefaultMongoValueObject doc = col.find(Filters.lte("bitemporalStamp.effectiveFrom.seconds", epocheseconds))
                .first();
        assertNotNull(doc);
    }

}
