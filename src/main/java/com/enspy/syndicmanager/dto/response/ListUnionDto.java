package com.enspy.syndicmanager.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListUnionDto {

    @JsonProperty("long_name")
    private String longName;

    @JsonProperty("legal_form")
    private String legalForm;

    @JsonProperty("short_name")
    private String shortName;

    private String email;
    private String description;

    @JsonProperty("business_domains")
    private List<String> businessDomains;
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

}
