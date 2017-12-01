package uk.gov.digital.ho.pttg;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Incomes {

    private final List<Income> income;
}


