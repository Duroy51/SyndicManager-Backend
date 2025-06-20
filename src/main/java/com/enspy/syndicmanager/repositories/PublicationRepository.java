package com.enspy.syndicmanager.repositories;

import com.enspy.syndicmanager.models.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, UUID> {
    List<Publication> findAllByOrderByCreatedAtDesc();
}
