package com.projectbarbel.histo.model;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;

import com.projectbarbel.histo.BarbelHistoHelper;
import com.projectbarbel.histo.model.BitemporalStamp.RecordPeriod;

/**
 * Value Objects in the application must implement this interface.
 * 
 * @author niklasschlimm
 *
 * @param <O> the unique object identifier type of the value object
 */
public interface Bitemporal<O> {

    public class EffectivePeriod {
        public final static LocalDate INFINITE = LocalDate.MAX;
        public LocalDate from;
        public LocalDate until;

        private EffectivePeriod() {
            super();
        }

        public static EffectivePeriod instance() {
            return new EffectivePeriod();
        }

        public EffectivePeriod from(LocalDate from) {
            this.from = from;
            return this;
        }

        public EffectivePeriod until(LocalDate until) {
            this.until = until;
            return this;
        }

        public EffectivePeriod toInfinite() {
            this.until = INFINITE;
            return this;
        }

        public EffectivePeriod fromNow() {
            this.from = LocalDate.now();
            return this;
        }
    }

    BitemporalStamp getBitemporalStamp();

    /**
     * The unique ID of the value object version (not the documentId). Must be
     * uniquie within the document collection/table.
     * 
     * @return version id
     */
    O getVersionId();

    default String getDocumentId() {
        return getBitemporalStamp().getDocumentId();
    }

    default Instant getEffectiveFromIntant() {
        return getBitemporalStamp().getEffectiveFrom();
    }

    default LocalDate getEffectiveFrom() {
        return BarbelHistoHelper.effectiveInstantToEffectiveDate(getEffectiveFromIntant());
    }

    default Instant getEffectiveUntilInstant() {
        return getBitemporalStamp().getEffectiveUntil();
    }

    default LocalDate getEffectiveUntil() {
        return BarbelHistoHelper.effectiveInstantToEffectiveDate(getEffectiveUntilInstant());
    }

    static Object flatCopyWithNewStamp(Object from, BitemporalStamp stamp) {
        Class<?> clazz = from.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Object to = null;
        try {
            to = clazz.newInstance();
            for (Field field : fields) {
                Field fieldFrom = from.getClass().getDeclaredField(field.getName());
                fieldFrom.setAccessible(true);
                Object value;
                if (fieldFrom.getType().isInstance(stamp)) {
                    value = stamp;
                } else {
                    value = fieldFrom.get(from);
                }
                Field fieldTo = to.getClass().getDeclaredField(field.getName());
                fieldTo.setAccessible(true);
                fieldTo.set(to, value);
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot instintiate class " + clazz.getName()
                    + " to create new Version. Make sure it has a public nullary contsructor!", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access class " + clazz.getName()
                    + " to create new Version. Make sure it has a public nullary contsructor!", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Cannot find field in class " + clazz.getName() + " to create new Version!", e);
        }
        return to;
    }

    @SuppressWarnings("unchecked")
    default <T extends Bitemporal<O>> T newVersion(String activity, String createdBy, EffectivePeriod period) {
        BitemporalStamp newStamp = BitemporalStamp.instance(activity, createdBy, getDocumentId(),
                EffectivePeriod.instance().from(getEffectiveFrom()).toInfinite(), RecordPeriod.valid());
        return (T) flatCopyWithNewStamp(this, newStamp);
    }

}
