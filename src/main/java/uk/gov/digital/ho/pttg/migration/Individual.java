package uk.gov.digital.ho.pttg.migration;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document
public class Individual {

    @JsonFormat(shape= JsonFormat.Shape.STRING)

    private String title;
    @NotNull
    private String forename;
    @NotNull
    private String surname;
    @NotNull
    private String nino;


    public Individual() {
    }

    public Individual(String title, String forename, String surname, String nino) {
        this.title = title;
        this.forename = forename;
        this.surname = surname;
        this.nino = nino;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNino() {
        return nino;
    }

    public void setNino(String nino) {
        this.nino = nino;
    }

}
