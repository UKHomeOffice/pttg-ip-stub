package uk.gov.digital.ho.pttg.migration;

import java.io.IOException;
import java.net.URISyntaxException;

public class BDDRunner {
    public static void main(String[] args) throws IOException, URISyntaxException {
        new MigrateBDDTable().migrate(args[0]);
    }
}
