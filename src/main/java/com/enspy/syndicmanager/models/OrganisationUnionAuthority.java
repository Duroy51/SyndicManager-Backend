package com.enspy.syndicmanager.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AUTHORITY")
public class OrganisationUnionAuthority extends OrganisationUnion {
}
