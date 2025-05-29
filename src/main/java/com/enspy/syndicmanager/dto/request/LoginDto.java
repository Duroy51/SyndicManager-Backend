package com.enspy.syndicmanager.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Builder
@Getter
@Setter
public class LoginDto {
    private String username;
    private String password;
}
