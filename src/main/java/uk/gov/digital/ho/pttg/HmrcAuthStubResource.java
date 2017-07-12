package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/oauth/token", produces = APPLICATION_JSON_VALUE)
public class HmrcAuthStubResource {
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public AuthToken auth(@RequestParam Map<String, String> body) throws UnsupportedEncodingException, HttpClientErrorException {
        log.info("auth called for client_Id={}, client_secret={}", body.get("client_id"), body.get("client_secret"));
        return new AuthToken();
    }
}
