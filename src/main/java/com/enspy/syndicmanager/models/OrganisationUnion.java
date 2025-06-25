package com.enspy.syndicmanager.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "union_type", discriminatorType = DiscriminatorType.STRING)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationUnion {

    private static final String DEFAULT_BUSINESS_DOMAIN = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "long_name", nullable = false)
    private String longName;

    @Column(name = "short_name", nullable = false, length = 50)
    private String shortName;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String mediaFolder;

    /**
     * Domaine d’activité fixé par défaut
     */
    @ElementCollection
    private List<String> businessDomains;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "legal_form")
    private String legalForm;

    @Column(name = "web-site_url")
    private String webSiteUrl;

    @Column(name = "social_network")
    private String socialNetwork;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "capital_share", precision = 19, scale = 2)
    private BigDecimal capitalShare;

    /**
     * Fixe à la date de création de l’entité
     */
    @CreationTimestamp
    @Column(name = "registration_date", updatable = false, nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "ceo_name")
    private String ceoName;

    @Column(name = "year_founded")
    private LocalDateTime yearFounded;


    @ElementCollection
    @Column(name = "keyword")
    private List<String> keywords;

    @Column(name = "number_of_employees")
    private Integer numberOfEmployees;

    @OneToMany(
            mappedBy = "organisationUnion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();

    /**
     * Initialise les valeurs par défaut avant insertion
     */
    @PrePersist
    private void prePersistDefaults() {
        if (businessDomains == null || businessDomains.isEmpty()) {
            businessDomains = Collections.singletonList(DEFAULT_BUSINESS_DOMAIN);
        }
    }

    /**
     * Ajoute une branche à cette organisation en gérant la relation bidirectionnelle
     */
    public void addBranch(Branch branch) {
        if (branch != null) {
            branch.setOrganisationUnion(this);
            this.branches.add(branch);
        }
    }

    /**
     * Retire une branche de cette organisation
     */
    public void removeBranch(Branch branch) {
        if (branch != null && this.branches.remove(branch)) {
            branch.setOrganisationUnion(null);
        }
    }
}
