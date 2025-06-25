package com.enspy.syndicmanager.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnionDto {

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

    private String type;

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
    private LocalDateTime registrationDate;

    @JsonProperty("ceo_name")
    private String ceoName;

    @JsonProperty("year_founded")
    private LocalDateTime yearFounded;

    @JsonProperty("branch_list")
    private List<BranchDto> branchList;

}
