package com.enspy.syndicmanager.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterDto {
    String username;
    String email;
    String password;
    @com.fasterxml.jackson.annotation.JsonProperty("first_name")
    String firstName;
    @com.fasterxml.jackson.annotation.JsonProperty("last_name")
    String lastName;
    String name;
    @com.fasterxml.jackson.annotation.JsonProperty("phone_number")
    String phoneNumber;
}
