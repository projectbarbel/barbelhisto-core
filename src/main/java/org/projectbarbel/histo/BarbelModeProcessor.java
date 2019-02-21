package org.projectbarbel.histo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import com.googlecode.cqengine.IndexedCollection;

public interface BarbelModeProcessor {

	<T> T drawMaiden(BarbelHistoContext context, T object);

	Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal sourceBitemporal,
			BitemporalStamp stamp);

	Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp);

	Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal);

	Object drawDocumentId(Object pojo);

	<T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(IndexedCollection<T> objects);

	<T> Collection<T> customPersistenceObjectsToManagedBitemporals(BarbelHistoContext context,
			Collection<Bitemporal> bitemporals);

	boolean validateManagedType(BarbelHistoContext context, Object candidate);

	Class<?> getPersistenceObjectType(Class<?> objectType);

	default <T> Optional<Object> getIdValue(T currentVersion) {
	    List<Field> fields = FieldUtils.getFieldsListWithAnnotation(currentVersion.getClass(), DocumentId.class);
	    Validate.isTrue(fields.size() == 1,
	            "cannot find document id - make sure exactly one field in the pojo is annotated with @DocumentId");
	    fields.get(0).setAccessible(true);
	    try {
	        return Optional.ofNullable(fields.get(0).get(currentVersion));
	    } catch (IllegalAccessException e) {
	        throw new IllegalStateException("no access permission when trying to receive document id on class: "
	                + currentVersion.getClass().getName(), e);
	    }
	}
	
}
