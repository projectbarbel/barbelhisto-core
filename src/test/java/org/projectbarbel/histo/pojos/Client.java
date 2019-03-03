package org.projectbarbel.histo.pojos;

import java.time.LocalDate;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(callSuper = false)
@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class Client {

    @DocumentId
    private String clientId;
    private String title;
    private String name;
    private String firstname;
    private LocalDate dateOfBirth;
    private Adress address;
    private String email;

}
