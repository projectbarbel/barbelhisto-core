package org.projectbarbel.histo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import com.googlecode.cqengine.IndexedCollection;

/**
 * Interface that {@link BarbelMode}s need to implement.
 * 
 * @author Niklas Schlimm
 *
 */
public interface BarbelModeProcessor {

	<T> T drawMaiden(BarbelHistoContext context, T object);

	Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal sourceBitemporal,
			BitemporalStamp stamp);

	Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp);

	Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal);

	Object drawDocumentId(Object pojo);
	
	<T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(Object id, IndexedCollection<T> objects);

	Bitemporal managedBitemporalToCustomPersistenceObject(Bitemporal bitemporal);

	<T> Collection<T> customPersistenceObjectsToManagedBitemporals(BarbelHistoContext context,
			Collection<Bitemporal> bitemporals);

	boolean validateManagedType(BarbelHistoContext context, Object candidate);

	Class<?> getPersistenceObjectType(Class<?> objectType);

	default <T> Optional<Object> getIdValue(T candidate) {
	    List<Field> fields = FieldUtils.getFieldsListWithAnnotation(candidate.getClass(), DocumentId.class);
	    Validate.isTrue(fields.size() == 1,
	            "cannot find document id - make sure exactly one field in the pojo is annotated with @DocumentId");
	    fields.get(0).setAccessible(true);
	    try {
	        return Optional.ofNullable(fields.get(0).get(candidate));
	    } catch (IllegalAccessException e) {
	        throw new IllegalStateException("no access permission when trying to receive document id on class: "
	                + candidate.getClass().getName(), e);
	    }
	}
	
	default String getStampFieldName(Class<?> candidateClass, Class<?> typeToSearch) {
        List<String> result = new ArrayList<>();
        Field[] fields = candidateClass.getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().equals(typeToSearch)) {
                result.add(f.getName());
            }
        }
        Validate.validState(result.size() == 1, "could not uniquely identify field of type " + typeToSearch.getName()
                + " - invalid candidate type: " + candidateClass.getName());
        return result.get(0);
    }
	
	String getDocumentIdFieldNameOnPersistedType(Class<?> candidate);

}
