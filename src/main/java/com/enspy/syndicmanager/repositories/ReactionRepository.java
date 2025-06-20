package com.enspy.syndicmanager.repositories;

import com.enspy.syndicmanager.models.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    List<Reaction> findByPublicationId(UUID publicationId);
    Optional<Reaction> findByUserIdAndPublicationIdAndReactionType(UUID userId, UUID publicationId, Reaction.ReactionType reactionType);
    List<Reaction> findByUserIdAndPublicationId(UUID userId, UUID publicationId);
    void deleteByUserIdAndPublicationIdAndReactionType(UUID userId, UUID publicationId, Reaction.ReactionType reactionType);
}
