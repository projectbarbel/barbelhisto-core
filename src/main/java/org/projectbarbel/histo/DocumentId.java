package org.projectbarbel.histo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to identify the primary key of a business type. Add this to the
 * unique Id of your business object like so:
 * 
 * <pre>
 * public class SomeBusinessPojo {
 *    <code>@DocumentId</code>
 *    private String documentId;
 *    ... any custom fields and methods
 *    public String getDocumentId() {
 *       return documentId;
 *    }
 *    public void setDocumentId(String id) {
 *       this.documentId=id;
 *    }
 * }
 * </pre>
 * 
 * <br>
 * 
 * @author Niklas Schlimm
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentId {
}
