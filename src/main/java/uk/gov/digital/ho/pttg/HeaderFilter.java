package uk.gov.digital.ho.pttg;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class HeaderFilter implements Filter {

    static final String USER_ID_HEADER = "userId";
    static final String REQUEST_ID_HEADER = "requestId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //does nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader(USER_ID_HEADER);
        String requestId = req.getHeader(REQUEST_ID_HEADER);

        try {
            // Setup MDC data:
            MDC.put(USER_ID_HEADER, StringUtils.isNotBlank(userId) ? userId : "Anonymous");
            MDC.put(USER_ID_HEADER, StringUtils.isNotBlank(requestId) ? requestId : "temp_requestId");
            chain.doFilter(request, response);
        } finally {
            MDC.remove(USER_ID_HEADER);
        }
    }

    @Override
    public void destroy() {
        //does nothing
    }
}
