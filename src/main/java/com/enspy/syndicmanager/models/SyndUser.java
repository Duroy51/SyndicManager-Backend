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
@Entity
@Table(name = "synd_user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyndUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String folder;



    /**
     * Branches auxquelles l'utilisateur appartient
     */
    @ManyToMany
    @JoinTable(
            name = "branch_members",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "branch_id")
    )
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();

    /**
     * Ajoute une branche à cet utilisateur et met à jour la relation bidirectionnelle
     */
    public void addBranch(Branch branch) {
        if (branch != null && !branches.contains(branch)) {
            branches.add(branch);
            branch.getMembers().add(this);
        }
    }

    /**
     * Retire une branche de cet utilisateur et met à jour la relation bidirectionnelle
     */
    public void removeBranch(Branch branch) {
        if (branch != null && branches.remove(branch)) {
            branch.getMembers().remove(this);
        }
    }
}
