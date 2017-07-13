package uk.gov.digital.ho.pttg.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.gov.digital.ho.pttg.*;
import uk.gov.digital.ho.pttg.Income;
import uk.gov.digital.ho.pttg.Individual;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Migrate {
    private DBCollection getApplicantCollection() {
        return new MongoClient().getDB("test").getCollection("applicants");
    }

    public void migrate() throws IOException, URISyntaxException {
        List<Applicant> migratedApplicants = readApplicants().stream().map((this::convert)).collect(toList());
        ObjectMapper objectMapper = getMapper();
        File directory = new File(getClass().getResource("/applicants/AA123456A.json").getPath()).getParentFile();

        List<ImmutablePair<String, String>> jsonFiles = migratedApplicants.stream().map(applicant -> toTuple(objectMapper, applicant)).collect(toList());
        jsonFiles.forEach((json) -> writeFile(directory, json.getLeft(), json.getRight()));

    }

    private void writeFile(File directory, String filename, String json) {
        try {
            System.out.print(".");
            IOUtils.write(json, new FileOutputStream(new File(directory, String.format("%s.json", filename.toUpperCase()))), "UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ImmutablePair<String, String> toTuple(ObjectMapper objectMapper, Applicant applicant)  {
        try {
            return new ImmutablePair<>(applicant.getIndividual().getNino(), objectMapper.writeValueAsString(applicant));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Applicant convert(Applicants applicant) {
        return new Applicant(getIndividual(applicant), getEmployments(applicant), getIncome(applicant));
    }

    private List<Income> getIncome(Applicants applicant) {
        return applicant.getIncomes().stream().map(income -> new Income(payeRefFromName(income.getEmployer()), new BigDecimal(income.getIncome()), BigDecimal.ZERO, income.getPayDate(), 1, null)).collect(toList());
    }

    private List<Employment> getEmployments(Applicants applicant) {
        Map<String, List<uk.gov.digital.ho.pttg.migration.Income>> employmentsWithIncomes = applicant.getIncomes().stream().collect(
                Collectors.groupingBy(uk.gov.digital.ho.pttg.migration.Income::getEmployer, Collectors.toList()));

        return employmentsWithIncomes.
                keySet().
                stream().
                map(employerName -> toEmployment(employerName, mapPayFequency(applicant.getPayFreq()), employmentsWithIncomes.get(employerName))).collect(toList());
    }

    private Employment toEmployment(String employerName, String frequency, List<uk.gov.digital.ho.pttg.migration.Income> incomes) {
        return new Employment(frequency, minDate(incomes), maxDate(incomes), toEmployer(employerName));
    }

    private String maxDate(List<uk.gov.digital.ho.pttg.migration.Income> incomes) {
        return incomes.stream().map(uk.gov.digital.ho.pttg.migration.Income::getPayDate).max(String.CASE_INSENSITIVE_ORDER).get();
    }

    private String minDate(List<uk.gov.digital.ho.pttg.migration.Income> incomes) {
        return incomes.stream().map(uk.gov.digital.ho.pttg.migration.Income::getPayDate).min(String.CASE_INSENSITIVE_ORDER).get();
    }

    private Employer toEmployer(String employerName) {
        return new Employer(payeRefFromName(employerName), employerName, someAddress());
    }

    private String payeRefFromName(String employerName) {
        return String.format("123/%s45678", shrink(employerName));
    }

    private Address someAddress() {
        return new Address("Electric Works", " Sheffield Digital Campus", "Concourse Way", "Sheffield", "South Yorkshire", "S1 2BJ");
    }

    private String shrink(String employerName) {
        return String.format("%c%c", employerName.charAt(0), employerName.charAt(employerName.length()-1)).toUpperCase();
    }

    private String mapPayFequency(String payFreq) {
        if (StringUtils.isBlank(payFreq)) {
            return "FOUR_WEEKLY";
        }
        switch (payFreq) {
            case "M1":
                return "MONTHLY";
            case "W1":
                return "WEEKLY";
        }
        return "FOUR_WEEKLY";
    }

    private Individual getIndividual(Applicants applicant) {
        uk.gov.digital.ho.pttg.migration.Individual individual = applicant.getIndividual();
        return new Individual(individual.getForename(), individual.getSurname(), individual.getNino(), "1980-01-31");
    }

    private List<Applicants> readApplicants() throws IOException {
        List<Applicants> applicants = new ArrayList<>();
        DBCursor cursor = getApplicantCollection().find();

        while(cursor.hasNext()) {
            String json = cursor.next().toString();
            applicants.add(new ObjectMapper().readValue(json, Applicants.class));
        }

        return applicants;
    }

    private ObjectMapper getMapper() {
        ObjectMapper m = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-M-d")));
        m.registerModule(javaTimeModule);
        m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        return m;
    }

}
