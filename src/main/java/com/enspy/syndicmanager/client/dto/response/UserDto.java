package com.enspy.syndicmanager.client.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("phone_number_verified")
    private boolean phoneNumberVerified;
}
