package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Individual  extends ResourceSupport {
    private String firstName;
    private String lastName;
    private String nino;
    private String dateOfBirth;
}
