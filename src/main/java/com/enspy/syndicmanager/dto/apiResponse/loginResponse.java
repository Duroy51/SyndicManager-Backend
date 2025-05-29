package com.enspy.syndicmanager.dto.apiResponse;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class loginResponse {
    private User user;
    private String token;
}
