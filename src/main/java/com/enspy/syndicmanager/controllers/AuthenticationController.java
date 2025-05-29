package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.dto.apiResponse.loginResponse;
import com.enspy.syndicmanager.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.enspy.syndicmanager.dto.request.LoginDto;
import reactor.core.publisher.Mono;

@RestController

public class AuthenticationController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Mono<loginResponse> createUser(@RequestBody LoginDto loginDto) {
        return this.authService.login(loginDto);
    }

}
