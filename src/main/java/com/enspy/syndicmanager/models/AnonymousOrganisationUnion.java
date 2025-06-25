package com.enspy.syndicmanager.models;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ANONYMOUS")
public class AnonymousOrganisationUnion extends OrganisationUnion {
}
