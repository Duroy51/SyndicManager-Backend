package com.enspy.syndicmanager.controllers;


import com.enspy.syndicmanager.client.dto.request.CreateOrganisationDto;
import com.enspy.syndicmanager.client.services.Organisation;
import com.enspy.syndicmanager.dto.request.UnionDto;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.services.UnionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class UnionController {

    private Organisation organisation;
    private UnionService unionService;

    @PostMapping("/create_union")
    public Mono<ResponseEntity<ResponseDto>> createUnion(
            @RequestBody CreateOrganisationDto request) {
        return organisation.createOrganisation(request)
                .map(responseDto ->
                        ResponseEntity
                                .status(responseDto.getStatus())        // récupère le code depuis le DTO
                                .body(responseDto)
                );
    }


    @GetMapping("/getAllUnions")
    public Mono<ResponseEntity<ResponseDto>> getAllUnions() {
        return organisation.getAllOrganisation()
                .map(responseDto ->
                        ResponseEntity
                                .status(responseDto.getStatus())        // récupère le code depuis le DTO
                                .body(responseDto)
                );
    }


    @PostMapping("/union/create")
    public Mono<ResponseEntity<ResponseDto>> createUnionTrue(
            @RequestBody UnionDto request) {
        return this.unionService.createUnion(request)
                .map(responseDto ->
                        ResponseEntity
                                .status(responseDto.getStatus())        // récupère le code depuis le DTO
                                .body(responseDto)
                );
    }


    @PostMapping("/union/all")
    public Mono<ResponseEntity<ResponseDto>> getAllUnion(
            ) {
        return this.unionService.getAllUnion()
                .map(responseDto ->
                        ResponseEntity
                                .status(responseDto.getStatus())        // récupère le code depuis le DTO
                                .body(responseDto)
                );
    }
}
