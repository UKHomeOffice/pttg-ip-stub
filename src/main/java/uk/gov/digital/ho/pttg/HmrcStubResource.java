package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/individuals", produces = APPLICATION_JSON_VALUE)
public class HmrcStubResource {
    private final ObjectMapper objectMapper;

    public HmrcStubResource(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RequestMapping(path = "/matching", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {"application/hal+json", "application/vnd.hmrc.P1.0+json"})
    public ResponseEntity<NoBodyResource> postMatchingFor(@RequestBody Identity identity, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpClientErrorException {
        log.info("match called for " + identity.getNino());
        if (!hasMatch(identity)) {
            log.info("no match found for " + identity.getNino());
            final ResponseEntity notFound = new ResponseEntity("There is no match for the information provided", HttpStatus.FORBIDDEN);
            return notFound;
        }
        return new ResponseEntity<NoBodyResource>(createMatchingNew(identityKey(identity), baseUrl(request)), HttpStatus.OK);
    }

    @RequestMapping(path = "/matching/{matchId}", method = RequestMethod.GET, produces = "application/vnd.hmrc.P1.0+json")
    public EmbeddedIndividual getMatchingFor(@PathVariable(name="matchId") String matchId, HttpServletRequest request) throws IOException {
        log.info("match GET called for " + matchId);
        return createIndividualNew(matchId, baseUrl(request));
    }

    @RequestMapping(path = "/income/", method = RequestMethod.GET, produces = "application/vnd.hmrc.P1.0+json")
    public NoBodyResource income(
            @RequestParam(name = "matchId") String matchId,
            HttpServletRequest request) throws IOException {
        log.info("income called with {} ", matchId);
        return createIncomeNew(matchId, baseUrl(request));
    }

    @RequestMapping(path = "/employments/", method = RequestMethod.GET, produces = "application/vnd.hmrc.P1.0+json")
    public NoBodyResource employment(
            @RequestParam(name = "matchId") String matchId,
            HttpServletRequest request) throws IOException {
        log.info("employment called with {} ", matchId);
        return createEmploymentNew(matchId, baseUrl(request));
    }

    @RequestMapping(path = "/income/paye", method = RequestMethod.GET, produces = "application/vnd.hmrc.P1.0+json")
    public PayeIncome incomePaye(
            @RequestParam(name="matchId") String matchId,
            @RequestParam(name = "fromDate") String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            HttpServletRequest request) throws IOException {
        log.info("incomesPaye called with {} fromDate={} toDate={}", matchId, fromDate, toDate);
        return createIncomesNew(matchId, baseUrl(request));
    }

    @RequestMapping(path = "/employments/paye", method = RequestMethod.GET, produces = "application/vnd.hmrc.P1.0+json")
    public Employments employmentPaye(
            @RequestParam(name="matchId") String matchId,
            @RequestParam(name = "fromDate") String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            HttpServletRequest request) throws IOException {
        log.info("employmentPaye called with {} fromDate={} toDate={}", matchId, fromDate, toDate);
        return createEmploymentsNew(matchId, baseUrl(request));
    }

    private EmbeddedIndividual createIndividualNew(String matchId, String baseUrl) throws IOException {
        String json = IOUtils.toString(getJsonResource(matchId), Charset.forName("UTF8"));
        EmbeddedIndividual individual = new EmbeddedIndividual(objectMapper.readValue(json, Applicant.class).getIndividual());
        individual.add(new Link(format("%s/individuals/%s", baseUrl, matchId)));
        individual.add(new Link(format("%s/individuals/employments/?matchId=%s", baseUrl, matchId), "employments"));
        individual.add(new Link(format("%s/individuals/income/?matchId=%s", baseUrl, matchId), "income"));
        return individual;
    }

    private NoBodyResource createMatchingNew(String matchId, String baseUrl) throws IOException {
        NoBodyResource individual = new NoBodyResource();
        individual.add(new Link(format("%s/individuals/matching/%s", baseUrl, matchId), "individual"));
        return individual;
    }

    private NoBodyResource createIncomeNew(String matchId, String baseUrl) throws IOException {
        NoBodyResource individual = new NoBodyResource();
        individual.add(new Link(format("%s/individuals/%s", baseUrl, matchId)));
        individual.add(new Link(format("%s/individuals/income/paye?matchId=%s", baseUrl, matchId), "paye"));
        return individual;
    }

    private PayeIncome createIncomesNew(String matchId, String baseUrl) throws IOException {
        String json = IOUtils.toString(getJsonResource(matchId), Charset.forName("UTF8"));
        return objectMapper.readValue(json, PayeIncome.class);
    }

    private NoBodyResource createEmploymentNew(String matchId, String baseUrl) throws IOException {
        NoBodyResource individual = new NoBodyResource();
        individual.add(new Link(format("%s/individuals/%s", baseUrl, matchId)));
        individual.add(new Link(format("%s/individuals/employments/paye?matchId=%s", baseUrl, matchId), "paye"));
        return individual;
    }

    private Employments createEmploymentsNew(String matchId, String baseUrl) throws IOException {
        String json = IOUtils.toString(getJsonResource(matchId), Charset.forName("UTF8"));
        return new Employments(objectMapper.readValue(json, Applicant.class).getEmployments());
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
