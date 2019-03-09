package org.projectbarbel.histo.suite.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.suite.context.BTTestPersitenceListenerOnly;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(BTTestPersitenceListenerOnly.class)
public @interface BTPersistenceListenerOnly {
}
