package com.enspy.syndicmanager.services;


import com.enspy.syndicmanager.dto.request.RegisterDto;
import com.enspy.syndicmanager.models.SyndUser;
import com.enspy.syndicmanager.repositories.SyndUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    SyndUserRepository syndUserRepository;
    public Mono<SyndUser> registerSyndicUser(RegisterDto registerDto, UUID id) {
        return Mono.fromCallable(() -> {
                    SyndUser user = new SyndUser();
                    user.setEmail(registerDto.getEmail());
                    user.setFirstName(registerDto.getFirstName());
                    user.setUsername(registerDto.getUsername());
                    user.setLastName(registerDto.getLastName());
                    user.setPassword(registerDto.getPassword());
                    user.setPhoneNumber(registerDto.getPhoneNumber());
                    user.setId(id);

                    return syndUserRepository.save(user);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> System.err.println("Erreur lors de la sauvegarde en base: " + error.getMessage()));
    }

}
