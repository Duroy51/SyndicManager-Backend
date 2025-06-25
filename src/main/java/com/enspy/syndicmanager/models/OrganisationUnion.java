package com.enspy.syndicmanager.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "union_type", discriminatorType = DiscriminatorType.STRING)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationUnion {
    @Id
    private UUID id;
}
