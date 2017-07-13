package uk.gov.digital.ho.pttg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryPoint {
    public final Links _links;
    @AllArgsConstructor
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private final Link match;
        @AllArgsConstructor
        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Link {
            private final String href;
        }
    }
}
