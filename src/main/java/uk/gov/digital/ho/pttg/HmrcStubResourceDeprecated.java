package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @deprecated replaced with HMRC version 1.0 in HmrcStubResource
 */
@Deprecated
@Slf4j
@RestController
@RequestMapping(value = "/individuals", produces = APPLICATION_JSON_VALUE)
public class HmrcStubResourceDeprecated {
    private final ObjectMapper objectMapper;

    public HmrcStubResourceDeprecated(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RequestMapping(path = "/{matchId}", method = RequestMethod.GET)
    public Individual individual(@PathVariable(name="matchId") String matchId, HttpServletRequest request) throws IOException {
        log.info("individual called with {}", matchId);
        return createIndividual(matchId, baseUrl(request));
    }

    @RequestMapping(path = "/{matchId}/employments/paye", method = RequestMethod.GET)
    public EmbeddedEmployments employments(
            @PathVariable(name="matchId") String matchId,
            @RequestParam(name = "fromDate") String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            HttpServletRequest request) throws IOException {
        log.info("employments called with {} fromDate={} toDate={}", matchId, fromDate, toDate);
        return createEmployments(matchId, baseUrl(request));
    }

    @RequestMapping(path = "/{matchId}/income/paye", method = RequestMethod.GET)
    public EmbeddedIncome icome(
            @PathVariable(name="matchId") String matchId,
            @RequestParam(name = "fromDate") String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            HttpServletRequest request) throws IOException {
        log.info("income called with {} fromDate={} toDate={}", matchId, fromDate, toDate);
        return createIncome(matchId, baseUrl(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public IndividualsEntryPoint entryPoint(HttpServletRequest request)
    {
        log.info("entry point called");
        return createEntryPoint(baseUrl(request));
    }

    @RequestMapping(path = "/match", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public String getMatchFor(@RequestBody Identity identity, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, HttpClientErrorException {
        log.info("match called for " + identity.getNino());
        if (!hasMatch(identity)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return notFoundBody();
        }
        response.setHeader("Location", format("%s/individuals/%s", baseUrl(request), identityKey(identity)));
        response.setStatus(HttpStatus.SEE_OTHER.value());
        return "";
    }

    private Individual createIndividual(String matchId, String baseUrl) throws IOException {
        String json = IOUtils.toString(getJsonResource(matchId), Charset.forName("UTF8"));
        Individual individual = objectMapper.readValue(json, Applicant.class).getIndividual();
        individual.add(new Link(format("%s/individuals/%s", baseUrl, matchId)));
        individual.add(new Link(format("%s/individuals/%s/employments/paye", baseUrl, matchId), "employments"));
        individual.add(new Link(format("%s/individuals/%s/income/paye", baseUrl, matchId), "income"));
        return individual;
    }


    private EmbeddedEmployments createEmployments(String matchId, String baseUrl) throws IOException {
        String json = IOUtils.toString(getJsonResource(matchId), Charset.forName("UTF8"));
        EmbeddedEmployments embeddedEmployments = new EmbeddedEmployments(objectMapper.readValue(json, Applicant.class).getEmployments());
        embeddedEmployments.add(new Link(format("%s/individuals/%s/employments/paye", baseUrl, matchId)));
        return embeddedEmployments;
    }

    private EmbeddedIncome createIncome(String matchId, String baseUrl) throws IOException {
        String json = IOUtils.toString(getJsonResource(matchId), Charset.forName("UTF8"));
        EmbeddedIncome embeddedIncome = new EmbeddedIncome(objectMapper.readValue(json, Applicant.class).getIncome());
        embeddedIncome.add(new Link(format("%s/individuals/%s/income/paye", baseUrl, matchId)));
        return embeddedIncome;
    }


    private String identityKey(Identity identity) {
        return identity.getNino();
    }

    private boolean hasMatch(Identity identity) {
        return getJsonResource(identity.getNino().toUpperCase()) != null;
    }

    private InputStream getJsonResource(String key) {
        return this.getClass().getResourceAsStream(format("/applicants/%s.json", key));
    }

    private IndividualsEntryPoint createEntryPoint(String baseUrl) {
        IndividualsEntryPoint individualsEntryPoint = new IndividualsEntryPoint();
        individualsEntryPoint.add(new Link(format("%s/individuals", baseUrl)));
        individualsEntryPoint.add(new Link(format("%s/individuals/match", baseUrl), "match"));
        return individualsEntryPoint;
    }

    private String baseUrl(HttpServletRequest request) {
        // HMRC do not return full url so we could have done this:
        //return format("%s://%s:%d",request.getScheme(),  request.getServerName(), request.getServerPort());
        // but instead just return blank
        return "";
    }

    private String notFoundBody() {
        return "{\n" +
                "    \"code\": \"MATCHING_FAILED\",\n" +
                "    \"message\": \"There is no match for the information provided.\"\n" +
                "}";
    }
}
