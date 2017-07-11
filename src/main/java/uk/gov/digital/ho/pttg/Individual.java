package uk.gov.digital.ho.pttg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

@Getter
@AllArgsConstructor
public class Individual  extends ResourceSupport {
    private String firstName;
    private String lastName;
    private String nino;
    private String dateOfBirth;
}
