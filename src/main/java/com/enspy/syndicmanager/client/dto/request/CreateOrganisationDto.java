package com.enspy.syndicmanager.client.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrganisationDto {

    @JsonProperty("long_name")
    private String longName;

    @JsonProperty("legal_form")
    private String legalForm;

    @JsonProperty("short_name")
    private String shortName;

    private String email;
    private String description;

    @JsonProperty("business_domains")
    @Builder.Default
    private List<UUID> businessDomains = List.of(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));

    @JsonProperty("logo_url")
    private String logoUrl;

    @Builder.Default
    private String type = "SOLE_PROPRIETORSHIP";

    @JsonProperty("web_site_url")
    private String webSiteUrl;

    @JsonProperty("social_network")
    private String socialNetwork;

    @JsonProperty("business_registration_number")
    private String businessRegistrationNumber;

    @JsonProperty("tax_number")
    private String taxNumber;

    @JsonProperty("capital_share")
    private Integer capitalShare;

    @JsonProperty("registration_date")
    private Instant registrationDate;

    @JsonProperty("ceo_name")
    private String ceoName;

    @JsonProperty("year_founded")
    private Instant yearFounded;


    // Informations relatives aux branches

}
