package uk.gov.digital.ho.pttg.dto;

import lombok.Data;
import org.springframework.hateoas.ResourceSupport;
import uk.gov.digital.ho.pttg.Income;

import java.util.List;

@Data
public class EmbeddedIncome extends ResourceSupport {
    private Embedded _embedded;

    @Data
    public static class Embedded {
        private List<Income> income;
    }
}