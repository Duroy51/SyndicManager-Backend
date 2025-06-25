package com.enspy.syndicmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch")
public class Branch {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private double longitude;
    private double latitude;
    private String mediaFolder;

    /**
     * Organisation parente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private OrganisationUnion organisationUnion;



    /**
     * Membres de la branche
     */
    @ManyToMany(mappedBy = "branches")
    @Builder.Default
    private List<SyndUser> members = new ArrayList<>();

    /**
     * Ajoute un membre à cette branche et met à jour la relation bidirectionnelle
     */
    public void addMember(SyndUser user) {
        if (user != null && !members.contains(user)) {
            members.add(user);
            user.getBranches().add(this);
        }
    }

    /**
     * Retire un membre de cette branche et met à jour la relation bidirectionnelle
     */
    public void removeMember(SyndUser user) {
        if (user != null && members.remove(user)) {
            user.getBranches().remove(this);
        }
    }
}