package com.onlineSchool.repository;

import com.onlineSchool.model.Comment;
import com.onlineSchool.model.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    
    List<Comment> findByEntityIdAndEntityType(Long entityId, EntityType entityType);
    
    List<Comment> findByUserId(Long userId);
    
    List<Comment> findByUserIdAndEntityTypeAndEntityId(Long userId, EntityType entityType, Long entityId);
    
    @Query("SELECT c FROM Comment c WHERE c.entityType = com.onlineSchool.model.EntityType.WEBINAR AND c.entityId = :webinarId")
    List<Comment> findByWebinarId(@Param("webinarId") Long webinarId);
    
    @Query("SELECT c FROM Comment c WHERE c.user.id = :authorId")
    List<Comment> findByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT c FROM Comment c WHERE c.entityType = com.onlineSchool.model.EntityType.WEBINAR AND c.entityId = :webinarId ORDER BY c.createdAt DESC")
    List<Comment> findLatestByWebinarId(@Param("webinarId") Long webinarId);

    boolean existsByIdAndUserId(Long id, Long userId);
} 