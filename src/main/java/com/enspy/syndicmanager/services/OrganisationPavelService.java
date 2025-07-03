package com.enspy.syndicmanager.services;

import com.enspy.syndicmanager.client.dto.response.OrganizationDto;
import com.enspy.syndicmanager.client.dto.response.AgencyDto;
import com.enspy.syndicmanager.client.services.OrganisationPavel;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class OrganisationPavelService {
    private final OrganisationPavel organisationPavel;

    // Liste des agences d’un syndicat (organisation)
    public Mono<ResponseDto> getAgencies(String organisationId) {
        return organisationPavel.getAgencies(organisationId)
                .flatMap(responseDto -> {
                    List<AgencyDto> agencies = (List<AgencyDto>) responseDto.getData();
                    if (agencies == null || agencies.isEmpty()) {
                        return createEmptyResponse();
                    }
                    return createSuccessResponse(agencies);
                })
                .onErrorResume(this::createErrorResponse);
    }

    // Afficher les informations d´etaillées d’un syndicat (organisation)
    public Mono<ResponseDto> getOrganisationInfoById(String organisationId, String informationId) {
        return organisationPavel.getOrganisationInfoById(organisationId, informationId)
                .flatMap(responseDto -> {
                    OrganizationDto info = (OrganizationDto) responseDto.getData();
                    if (info == null) {
                        return createEmptyResponse();
                    }
                    return createSuccessResponse(info);
                })
                .onErrorResume(this::createErrorResponse);
    }

    // Méthodes utilitaires pour les réponses
    private Mono<ResponseDto> createEmptyResponse() {
        ResponseDto response = new ResponseDto();
        response.setData(Collections.emptyList());
        response.setStatus(204);
        return Mono.just(response);
    }

    private Mono<ResponseDto> createSuccessResponse(Object data) {
        ResponseDto response = new ResponseDto();
        response.setData(data);
        response.setStatus(200);
        return Mono.just(response);
    }

    private Mono<ResponseDto> createErrorResponse(Throwable ex) {
        ResponseDto r = new ResponseDto();
        r.setStatus(500);
        r.setText("Erreur interne : " + ex.getMessage());
        return Mono.just(r);
    }
}
