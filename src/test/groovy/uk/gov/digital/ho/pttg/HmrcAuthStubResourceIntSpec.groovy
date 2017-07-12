package uk.gov.digital.ho.pttg

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration
@SuppressFBWarnings
class HmrcAuthStubResourceIntSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    def jsonSlurper = new JsonSlurper()


    def 'should return access token for a auth request'() {
        given:
        def clientId = 'agagdiagidaid'
        def totpCode = 'skhaskfhksfksfs'

        when:
        def headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", totpCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        def response = restTemplate.postForEntity("/oauth/token", request, String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        body.access_token =~ /.+/
        body.token_type == 'bearer'
        body.scope =~ /.+/
        body.expires_in == 14400
    }

}



