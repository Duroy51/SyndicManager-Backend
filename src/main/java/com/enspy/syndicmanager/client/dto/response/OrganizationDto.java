package com.enspy.syndicmanager.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("business_actor_id")
    private String businessActorId;

    @JsonProperty("business_domains")
    private List<String> businessDomains;

    private String email;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("long_name")
    private String longName;

    private String description;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("is_individual_business")
    private boolean isIndividualBusiness;

    @JsonProperty("legal_form")
    private String legalForm;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("website_url")
    private String websiteUrl;

    @JsonProperty("social_network")
    private String socialNetwork;

    @JsonProperty("business_registration_number")
    private String businessRegistrationNumber;

    @JsonProperty("tax_number")
    private String taxNumber;

    @JsonProperty("capital_share")
    private int capitalShare;

    @JsonProperty("registration_date")
    private LocalDateTime registrationDate;

    @JsonProperty("ceo_name")
    private String ceoName;

    @JsonProperty("year_founded")
    private LocalDateTime yearFounded;

    private List<String> keywords;

    private String status;
}
