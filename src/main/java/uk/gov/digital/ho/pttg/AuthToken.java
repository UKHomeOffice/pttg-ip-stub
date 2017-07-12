package uk.gov.digital.ho.pttg;

import java.util.UUID;

public class AuthToken {
    public String getAccess_token() {
        return UUID.randomUUID().toString();
    }

    public String getToken_type() {
        return "bearer";
    }

    public String getScope() {
        return "scope1, scope2";
    }

    public int getExpires_in() {
        return 14400;
    }

}
