package uk.gov.digital.ho.pttg.migration;

import java.io.IOException;
import java.net.URISyntaxException;

public class Runner {
    public static void main(String[] args) throws IOException, URISyntaxException {
        new Migrate().migrate();
    }
}
