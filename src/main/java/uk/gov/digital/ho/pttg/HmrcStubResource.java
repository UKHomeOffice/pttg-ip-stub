package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping("/individuals")
public class HmrcStubResource {

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public IndividualsEntryPoint entryPoint(HttpServletRequest request)
    {
        log.info("entry point called");
        return createEntryPoint(baseUrl(request));
    }

    private IndividualsEntryPoint createEntryPoint(String baseUrl) {
        IndividualsEntryPoint individualsEntryPoint = new IndividualsEntryPoint();
        individualsEntryPoint.add(new Link(format("%s/individuals", baseUrl)));
        individualsEntryPoint.add(new Link(format("%s/individuals/match", baseUrl), "match"));
        return individualsEntryPoint;
    }

    private String baseUrl(HttpServletRequest request) {
        return format("%s://%s:%d",request.getScheme(),  request.getServerName(), request.getServerPort());
    }
}
