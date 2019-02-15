package org.projectbarbel.histo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to use in {@link BarbelMode#POJO} which is the default mode. Add
 * this to the unique Id of your business object like so:
 * 
 * <pre>
 * public class SomeBusinessPojo {
 *    <code>@DocumentId</code>
 *    private String documentId;
 *    ... any custom fields and methods
 *    public String getDocumentId() {
 *       return documentId;
 *    }
 * }
 * </pre>
 * 
 * Pojos must adhere to the JavaBeans specification. In short: they should have
 * accessor methods to all their fields and implement a default constructor.<br>
 * <br>
 * 
 * @author Niklas Schlimm
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentId {
}
