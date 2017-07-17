package uk.gov.digital.ho.pttg.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.TotpGenerator;
import uk.gov.digital.ho.pttg.dto.AccessToken;
import uk.gov.digital.ho.pttg.dto.EmbeddedEmployments;
import uk.gov.digital.ho.pttg.dto.EmbeddedIncome;
import uk.gov.digital.ho.pttg.dto.EntryPoint;
import uk.gov.digital.ho.pttg.dto.MatchedIndividual;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration
@RunWith(SpringRunner.class)
public class HappyPathTest {
    private static final ParameterizedTypeReference<Resource<MatchedIndividual>> matchedIndividualTypeRef = new ParameterizedTypeReference<Resource<MatchedIndividual>>() {
    };
    private static final ParameterizedTypeReference<Resource<EmbeddedIncome>> incomeTypeRef = new ParameterizedTypeReference<Resource<EmbeddedIncome>>() {
    };
    private static final ParameterizedTypeReference<Resource<EmbeddedEmployments>> employmentTypeRef = new ParameterizedTypeReference<Resource<EmbeddedEmployments>>() {
    };

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void employmentAndIncomeDetails() throws URISyntaxException, InvalidKeyException, NoSuchAlgorithmException {
        String accessToken = getAccessToken("tMEV8jBnmqotNufIgBUNIz_QfhYa", TotpGenerator.getTotpCode("IAGVQR33EVGGSZYH"));
        String matchUrl = getMatchUrl(accessToken);
        Resource<MatchedIndividual> individualMatchUrl = getIndividualMatchUrl(matchUrl, accessToken);
        EmbeddedEmployments employments =
                followTraverson(withDateRange(individualMatchUrl.getLink("employments").getHref()), accessToken)
                        .toObject(employmentTypeRef)
                        .getContent();
        EmbeddedIncome income =
                followTraverson(withDateRange(individualMatchUrl.getLink("income").getHref()), accessToken)
                        .toObject(incomeTypeRef)
                        .getContent();

        Assertions.assertThat(employments.get_embedded().getEmployments()).hasSize(1);
        Assertions.assertThat(income.get_embedded().getIncome()).hasSize(7);
    }

    private String withDateRange(String href) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(href);
        List<NameValuePair> templateParameters = ImmutableList.copyOf(builder.getQueryParams());
        builder.clearParameters();
        builder.addParameter(findParameterName("from", templateParameters), "2016-01-31");
        builder.addParameter(findParameterName("to", templateParameters), "2016-06-01");
        return builder.build().toASCIIString();
    }

    private String findParameterName(String matchName, List<NameValuePair> templateParameters) {
        return templateParameters
                .stream()
                .filter(parameter -> parameter.getName().toLowerCase().contains(matchName))
                .findAny()
                .map(NameValuePair::getName)
                .orElseThrow(() -> new RuntimeException("Don't know what parameter name should be"));
    }

    private Resource<MatchedIndividual> getIndividualMatchUrl(String matchUrl, String accessToken) throws URISyntaxException {
        ResponseEntity<String> entity = restTemplate.postForEntity(matchUrl, createEntity(identityDetails(), accessToken), String.class);
        String location = entity.getHeaders().get("Location").get(0);
        return followTraverson(location, accessToken).toObject(matchedIndividualTypeRef);
    }

    private String getMatchUrl(String accessToken) {
        EntryPoint json = restTemplate.exchange("/individuals", HttpMethod.GET,  createEntity(null, accessToken), EntryPoint.class).getBody();
        return json.get_links().getMatch().getHref();
    }

    private String getAccessToken(String clientId, String totpCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", totpCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<AccessToken> response = restTemplate.postForEntity("/oauth/token", request, AccessToken.class);
        return response.getBody().getAccess_token();
    }

    private String identityDetails() {
        return "{\n" +
                "\"firstName\": \"Bobby\",\n" +
                "\"lastName\": \"Beans\",\n" +
                "\"nino\": \"TT123456T\",\n" +
                "\"dateOfBirth\": \"1980-01-13\"\n" +
                "}";
    }

    private static HttpHeaders generateRestHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", APPLICATION_JSON_VALUE);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private static HttpEntity createEntity(Object entity, String accessToken) {
        return new HttpEntity<>(entity, generateRestHeaders(accessToken));
    }

    private static Traverson traversonFor(String link) throws URISyntaxException {
        return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(new RestTemplate(Collections.singletonList(getHalConverter())));
    }

    private static Traverson.TraversalBuilder followTraverson(String link, String accessToken) throws URISyntaxException {
        return traversonFor(link).follow().withHeaders(generateRestHeaders(accessToken));
    }

    private static HttpMessageConverter<?> getHalConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jackson2HalModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        return converter;
    }
}
