package uk.gov.digital.ho.pttg.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Applicants {

    private String id;

    @NotNull
    private Individual individual;

    @NotNull
    private String payFreq;

    //@NotNull
    private String accountNumber;

    private List<Income> incomes;

    public Applicants() {
    }

    public Applicants(String id, Individual individual, String payFreq, String accountNumber, List<Income> incomes) {
        this.id = id;
        this.individual = individual;
        this.payFreq = payFreq;
        this.accountNumber = accountNumber;
        this.incomes = incomes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public String getPayFreq() {
        return payFreq;
    }

    public void setPayFreq(String payFreq) {
        this.payFreq = payFreq;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public List<Income> getIncomes() {
        return incomes;
    }

    public void setIncomes(List<Income> incomes) {
        this.incomes = incomes;
    }

}
