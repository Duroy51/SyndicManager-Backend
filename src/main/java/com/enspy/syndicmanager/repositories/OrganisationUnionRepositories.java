package com.enspy.syndicmanager.repositories;

import com.enspy.syndicmanager.models.OrganisationUnion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganisationUnionRepositories extends JpaRepository<OrganisationUnion, UUID> {

}
