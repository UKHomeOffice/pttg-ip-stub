package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Applicant {
    private Individual individual;
    private List<Employment> employments;
    private PayeIncome paye;
}
