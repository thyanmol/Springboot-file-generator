package com.example.service.dto;

import java.io.Serializable;
public class userProfileDTO implements Serializable {

    private String firstName;

    private String email;

    private Long id;
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
