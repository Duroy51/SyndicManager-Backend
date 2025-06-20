package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.client.dto.response.LoginResponse;
import com.enspy.syndicmanager.client.services.Authentication;
import com.enspy.syndicmanager.dto.request.RegisterDto;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.enspy.syndicmanager.dto.request.LoginDto;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class AuthenticationController {

    private AuthService authService;

    private Authentication authentication;

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginDto loginDto) {
        return this.authentication.login(loginDto);
    }

    @PostMapping("/register")
    public Mono<ResponseDto> createUser(@RequestBody RegisterDto registerDto) {
        return this.authentication.register(registerDto);
    }

}
