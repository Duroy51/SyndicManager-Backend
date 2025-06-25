package com.enspy.syndicmanager.repositories;

import com.enspy.syndicmanager.models.OrganisationUnion;
import com.enspy.syndicmanager.models.SyndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyndUserRepository extends JpaRepository<SyndUser, UUID> {
    @Override
    Optional<SyndUser> findById(UUID uuid);
}
