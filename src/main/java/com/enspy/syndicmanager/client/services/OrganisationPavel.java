package com.enspy.syndicmanager.client.services;

import com.enspy.syndicmanager.client.dto.request.CreateOrganisationDto;
import com.enspy.syndicmanager.client.dto.response.AgencyDto;
import com.enspy.syndicmanager.client.dto.response.OrganizationDto;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class OrganisationPavel {

    ApiService apiService;

    // Liste des agences (antennes) d’un syndicat
    public Mono<ResponseDto> getAgencies(String organisationId) {
        String endpoint = "/organization-service/organizations/" + organisationId + "/agencies";
        ParameterizedTypeReference<AgencyDto> typeRef = new ParameterizedTypeReference<AgencyDto>() {};
        return apiService.sendRequest(HttpMethod.GET, endpoint, null, typeRef);
    }

    // Afficher les informations détaillées d’une organisation(syndicat)
    public Mono<ResponseDto> getOrganisationInfoById(String organisationId, String informationId) {
        String endpoint = "/organization-service/organizations/" + organisationId + "/practical-infos/" + informationId;
        ParameterizedTypeReference<OrganizationDto> typeRef = new ParameterizedTypeReference<OrganizationDto>() {};
        return apiService.sendRequest(HttpMethod.GET, endpoint, null, typeRef);
    }



}
