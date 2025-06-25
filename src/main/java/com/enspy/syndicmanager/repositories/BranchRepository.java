package com.enspy.syndicmanager.repositories;


import com.enspy.syndicmanager.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    @Override
    Optional<Branch> findById(UUID uuid);
}
