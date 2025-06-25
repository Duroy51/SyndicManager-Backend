package com.enspy.syndicmanager.client.services;


import com.enspy.syndicmanager.client.dto.request.CreateOrganisationDto;
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
public class Organisation {

    ApiService apiService;

    public Mono<ResponseDto> createOrganisation(CreateOrganisationDto request){
        ParameterizedTypeReference<Void> voidTypeRef =
                new ParameterizedTypeReference<Void>() {};
        String endpoint  = "/organization-service/organizations";
        HttpMethod method = HttpMethod.POST;


        return apiService.sendRequest(method, endpoint, request, voidTypeRef );
    }

    public Mono<ResponseDto> getAllOrganisation(){
        ParameterizedTypeReference<List<OrganizationDto>> listOrganizationTypeRef =
                new ParameterizedTypeReference<List<OrganizationDto>>() {};

        String endpoint  = "/organization-service/organizations";
        HttpMethod method = HttpMethod.GET;

        return apiService.sendRequest(method, endpoint, null,listOrganizationTypeRef );
    }
}
