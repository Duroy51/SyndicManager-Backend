package com.enspy.syndicmanager.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterDto {
    String username;
    String email;
    String password;
    String first_name;
    String last_name;
    String name;
    String phone_number;
}
