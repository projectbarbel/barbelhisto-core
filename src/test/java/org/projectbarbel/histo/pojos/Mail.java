package org.projectbarbel.histo.pojos;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Mail {

    public final String mailNumber;
    public final Map<String, Object> mailParameter;
    public final String mailTo;

}
