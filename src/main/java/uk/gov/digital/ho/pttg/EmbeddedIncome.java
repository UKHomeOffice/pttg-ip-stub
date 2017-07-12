package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmbeddedIncome extends ResourceSupport{
    @JsonIgnore
    private final List<Income> income;

    public Embedded get_embedded() {
        return new Embedded(income);
    }

    @Getter
    @AllArgsConstructor
    private static class Embedded {
        private final List<Income> income;
    }
}


