package com.enspy.syndicmanager.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACCREDITED")
public class AccreditedOrganisationUnion extends OrganisationUnion {
}
