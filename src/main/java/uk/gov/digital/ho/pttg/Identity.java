package uk.gov.digital.ho.pttg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Identity {
    private String firstName;
    private String lastName;
    private String nino;
    private String dateOfBirth;
}
