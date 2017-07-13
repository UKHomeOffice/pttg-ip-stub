package uk.gov.digital.ho.pttg.migration;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotNull;

public class Income {

    @JsonFormat(shape= JsonFormat.Shape.STRING)
    @NotNull
    private String payDate;
    @NotNull
    private String income;
    @NotNull
    private String employer;


    public Income() {
    }

    public Income(String payDate, String income, String employer) {
        this.payDate = payDate;
        this.income = income;
        this.employer = employer;
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

}
