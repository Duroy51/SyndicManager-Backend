package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.services.OrganisationPavelService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/organisations")
@RequiredArgsConstructor
public class OrganisationPavelController {
    private final OrganisationPavelService organisationPavelService;


    //  liste des agences d’un syndicat

    @GetMapping("/{organisationId}/agencies")
    public Mono<ResponseDto> getAgencies(@PathVariable String organisationId) {
        return organisationPavelService.getAgencies(organisationId);
    }

    //  Afficher les informations détaillées d’un syndicat (organisation)

    @GetMapping("/{organisationId}/practical-infos/{informationId}")
    public Mono<ResponseDto> getOrganisationInfoById(@PathVariable String organisationId, @PathVariable String informationId) {
        return organisationPavelService.getOrganisationInfoById(organisationId, informationId);
    }
}

