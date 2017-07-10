package uk.gov.digital.ho.pttg

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration
class HmrcStubResourceIntSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    def jsonSlurper = new JsonSlurper()


    def 'entry point should return self'() {
        when:
        def response = restTemplate.getForEntity("/individuals", String.class)
        then:
        def entryPoint = jsonSlurper.parseText(response.body)
        entryPoint._links.self.href =~ /http:\/\/localhost:[0-9]+\/individuals/
    }

    def 'entry point should return match'() {
        when:
        def response = restTemplate.getForEntity("/individuals", String.class)
        then:
        def entryPoint = jsonSlurper.parseText(response.body)
        entryPoint._links.match.href =~ /http:\/\/localhost:[0-9]+\/individuals\/match/
    }

}



