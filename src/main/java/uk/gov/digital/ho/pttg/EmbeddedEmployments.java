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
public class EmbeddedEmployments extends ResourceSupport{
    @JsonIgnore
    private final List<Employment> employments;

    public Embedded get_embedded() {
        return new Embedded(employments);
    }

    @Getter
    @AllArgsConstructor
    private static class Embedded {
        private final List<Employment> employments;
    }
}


