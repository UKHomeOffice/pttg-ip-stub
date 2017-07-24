package uk.gov.digital.ho.pttg

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration
@SuppressFBWarnings
class HmrcStubResourceIntSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    def jsonSlurper = new JsonSlurper()
    def jsonBuilder = new JsonBuilder()


    def 'entry point should return self'() {
        when:
        def response = restTemplate.exchange("/individuals", HttpMethod.GET,  createEntity(null), String.class)
        then:
        def entryPoint = jsonSlurper.parseText(response.body)
        entryPoint._links.self.href =~ /\/individuals/
    }

    def 'entry point should return match'() {
        when:
        def response = restTemplate.exchange("/individuals", HttpMethod.GET,  createEntity(null), String.class)
        then:
        def entryPoint = jsonSlurper.parseText(response.body)
        entryPoint._links.match.href =~ /\/individuals\/match/
    }

    def 'should return 303 when matches'() {
        given:
        String matchingNino = matchingNino()
        when:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino
            dateOfBirth '1980-01-13'
        }

        def response = restTemplate.postForEntity("/individuals/match", createEntity(jsonBuilder.toString()), String.class)
        then:
        response.statusCode == HttpStatus.SEE_OTHER
    }

    def 'should return match location when matches'() {
        when:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def response = restTemplate.postForEntity("/individuals/match", createEntity(jsonBuilder.toString()), String.class)
        then:
        response.headers['Location'][0] =~ /\/individuals\/+./
    }

    def 'should return 403 when no matches'() {
        when:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino nonMatchingNino()
            dateOfBirth '1980-01-13'
        }
        def response = restTemplate.postForEntity("/individuals/match", createEntity(jsonBuilder.toString()), String.class)
        then:
        response.statusCode == HttpStatus.FORBIDDEN
    }

    def 'should return individual details for matched individual'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def individualUrl = getIndividualUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(individualUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        body.nino == matchingNino()
        body.dateOfBirth == '1980-01-13'
        body.firstName == 'Bobby'
        body.lastName == 'Beans'
    }

    def 'should return employments link for matched individual'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def individualUrl = getIndividualUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(individualUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        body._links.employments.href =~ /\/individuals\/.+\/employments\/paye/
    }
    def 'should return income link for matched individual'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def individualUrl = getIndividualUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(individualUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        body._links.income.href =~ /\/individuals\/.+\/income\/paye/
    }

    def 'should return list of employments when following matched link'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def employmentsUrl = getEmploymentsUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(employmentsUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        body._embedded.employments.size == 1
    }

    def 'should return employment details when following matched link'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def employmentsUrl = getEmploymentsUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(employmentsUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        def employment = body._embedded.employments[0]
        employment.payFrequency == 'MONTHLY'
        employment.startDate == '2014-06-15'
        employment.endDate == '2015-01-15'
        employment.employer.payeReference == "123/AI45678"
        employment.employer.name == "Flying Pizza Ltd"
        employment.employer.address.line1 == "Electric Works"
        employment.employer.address.line2 == "Sheffield Digital Campus"
        employment.employer.address.line3 == "Concourse Way"
        employment.employer.address.line4 == "Sheffield"
        employment.employer.address.line5 == "South Yorkshire"
        employment.employer.address.postcode == "S1 2BJ"
    }

    def 'should return list of incomes when following matched link'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def employmentsUrl = getIncomeUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(employmentsUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        body._embedded.income.size == 7
    }

    def 'should return income details when following matched link'() {
        given:
        jsonBuilder {
            firstName 'Bobby'
            lastName 'Beans'
            nino matchingNino()
            dateOfBirth '1980-01-13'
        }

        def employmentsUrl = getIncomeUrlFor(jsonBuilder.toString())

        when:
        def response = restTemplate.exchange(employmentsUrl, HttpMethod.GET,  createEntity(null), String.class)

        then:
        def body = jsonSlurper.parseText(response.body)
        def income = body._embedded.income[0]
        def incomeLast = body._embedded.income[6]
        income.employerPayeReference == '123/AI45678'
        income.taxablePayment == 1600.00
        income.nonTaxablePayment == 100.25
        income.paymentDate == '2014-06-15'
        income.monthPayNumber == 3
        incomeLast.weekPayNumber == 31
    }

    def getIndividualUrlFor(String individual) {
        def response = restTemplate.postForEntity("/individuals/match", createEntity(individual), String.class)
        return response.headers['Location'][0]
    }

    def getEmploymentsUrlFor(String individual) {
        def response = restTemplate.exchange(getIndividualUrlFor(individual), HttpMethod.GET,  createEntity(null), String.class)
        def body = jsonSlurper.parseText(response.body)
        return body._links.employments.href + '?fromDate=2016-01-01'
    }

    def getIncomeUrlFor(String individual) {
        def response = restTemplate.exchange(getIndividualUrlFor(individual), HttpMethod.GET,  createEntity(null), String.class)
        def body = jsonSlurper.parseText(response.body)
        return body._links.income.href + '?fromDate=2016-01-01'
    }

    def matchingNino() {
        return "TT123456T"
    }

    def nonMatchingNino() {
        return "ZZ999999Z"
    }

    def generateRestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add('Content-Type', APPLICATION_JSON_VALUE)
        return headers;
    }

    def createEntity(Object entity) {
        return new HttpEntity<>(entity, generateRestHeaders());
    }

}



