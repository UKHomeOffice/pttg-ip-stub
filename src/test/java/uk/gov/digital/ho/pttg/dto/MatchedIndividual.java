package uk.gov.digital.ho.pttg.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@AllArgsConstructor
@Setter
@Getter
@JsonInclude(NON_NULL)
public class MatchedIndividual {
    private String firstName;
    private String lastName;
    private String nino;
    private String dateOfBirth;
}
