package com.enspy.syndicmanager.repositories;

import com.enspy.syndicmanager.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPublicationId(UUID publicationId);
    List<Comment> findByPublicationIdAndParentIdIsNull(UUID publicationId);
    List<Comment> findByParentId(UUID parentId);
    List<Comment> findByUserId(UUID userId);
}
