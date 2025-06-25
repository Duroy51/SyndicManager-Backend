package com.enspy.syndicmanager.services;


import com.enspy.syndicmanager.client.dto.request.CreateOrganisationDto;
import com.enspy.syndicmanager.client.tokenHandler.TokenContextUtils;
import com.enspy.syndicmanager.dto.request.BranchDto;
import com.enspy.syndicmanager.dto.request.UnionDto;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Branch;
import com.enspy.syndicmanager.models.OrganisationUnion;
import com.enspy.syndicmanager.models.SyndUser;
import com.enspy.syndicmanager.repositories.OrganisationUnionRepositories;
import com.enspy.syndicmanager.repositories.SyndUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UnionService {

    OrganisationUnionRepositories organisationUnionRepositories;
    TokenContextUtils tokenContextUtils;
    SyndUserRepository syndUserRepository;
    StorageService storageService;

    public Mono<ResponseDto> createUnion(UnionDto organisationDto) {
       
        Mono<SyndUser> creatorMono = TokenContextUtils.getCurrentUserId()
                .map(UUID::fromString)
                .flatMap(id ->
                        Mono.justOrEmpty(syndUserRepository.findById(id))
                                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")))
                );


        return creatorMono
                .flatMap(creator ->
                        Mono.fromCallable(() -> {
                                    // Création de l'union
                                    OrganisationUnion union = new OrganisationUnion();
                                    union.setLongName(organisationDto.getLongName());
                                    union.setShortName(organisationDto.getShortName());
                                    union.setEmail(organisationDto.getEmail());
                                    union.setLegalForm(organisationDto.getLegalForm());
                                    union.setDescription(organisationDto.getDescription());
                                    union.setWebSiteUrl(organisationDto.getWebSiteUrl());
                                    union.setCeoName(organisationDto.getCeoName());
                                    union.setBusinessDomains(organisationDto.getBusinessDomains());

                                    // Création des branches avec l'utilisateur (maintenant disponible)
                                    for (BranchDto branchDto : organisationDto.getBranchList()) {
                                        Branch branch = new Branch();
                                        branch.setName(branchDto.getName());
                                        branch.setLatitude(branchDto.getLatitude());
                                        branch.setLongitude(branchDto.getLongitude());
                                        branch.addMember(creator); // creator est maintenant un SyndUser
                                        union.addBranch(branch);
                                    }

                                    // Sauvegarde synchrone encapsulée
                                    return organisationUnionRepositories.save(union);
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(savedUnion -> {
                    ResponseDto response = new ResponseDto();
                    response.setStatus(200);
                    response.setText("Organisation created successfully");
                    return response;
                })
                .onErrorMap(throwable -> {
                    ResponseDto errorResponse = new ResponseDto();
                    errorResponse.setStatus(500);
                    errorResponse.setText("Error creating organisation: " + throwable.getMessage());
                    return new RuntimeException("Failed to create union", throwable);
                });
    }


    public Mono<ResponseDto> getAllUnion() {
        return Mono.fromCallable(() -> organisationUnionRepositories.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(allUnion -> {
                    if (allUnion.isEmpty()) {
                        return createEmptyResponse();
                    }

                    // Conversion de la liste en utilisant des streams
                    List<UnionDto> allUnionDto = allUnion.stream()
                            .map(union -> {
                                UnionDto unionDto = new UnionDto();
                                unionDto.setLongName(union.getLongName());
                                unionDto.setShortName(union.getShortName());
                                unionDto.setCeoName(union.getCeoName());
                                unionDto.setEmail(union.getEmail());
                                unionDto.setDescription(union.getDescription());
                                unionDto.setLegalForm(union.getLegalForm());
                                unionDto.setLogoUrl(union.getLogoUrl());
                                unionDto.setSocialNetwork(union.getSocialNetwork());
                                unionDto.setYearFounded(union.getYearFounded());
                                unionDto.setWebSiteUrl(union.getWebSiteUrl());
                                unionDto.setRegistrationDate(union.getRegistrationDate());

                                return unionDto;
                            })
                            .collect(Collectors.toList());

                    return createSuccessReponse(allUnionDto);
                })
                .onErrorResume(ex -> {
                    System.err.println("Erreur lors de la récupération des unions: " + ex.getMessage());
                    return createErrorResponse(ex);
                });
    }

    public Mono<ResponseDto> uploadLogo(MultipartFile file, UUID unionId){
        Optional<OrganisationUnion> union = organisationUnionRepositories.findById(unionId);
        if(union.isEmpty()){
           return createEmptyResponse();
        } else {
            this.storageService.saveUnionLogo(file, unionId);
            return createSuccessReponse(null);
        }
    }





    private Mono<ResponseDto> createEmptyResponse() {
        ResponseDto response = new ResponseDto();
        response.setData(Collections.emptyList());
        response.setStatus(204);
        return Mono.just(response);
    }

    private Mono<ResponseDto> createSuccessReponse(Object data) {
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
