package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmbeddedIncome extends ResourceSupport{
    @JsonIgnore
    private final List<Income> income;

    public Embedded get_embedded() {
        return new Embedded(income);
    }

    @Getter
    @AllArgsConstructor
    private class Embedded {
        private final List<Income> income;
    }
}


