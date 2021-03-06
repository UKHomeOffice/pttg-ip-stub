package uk.gov.digital.ho.pttg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Employer {
    private final String payeReference;
    private final String name;
    private final Address address;
}
