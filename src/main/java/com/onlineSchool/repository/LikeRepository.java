package com.onlineSchool.repository;

import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    
    List<Like> findByUserId(Long userId);
    
    List<Like> findByUserIdAndEntityTypeAndEntityId(Long userId, EntityType entityType, Long entityId);
    
    boolean existsByIdAndUserId(Long id, Long userId);
    
    Optional<Like> findByUserIdAndEntityIdAndEntityType(Long userId, Long entityId, EntityType entityType);
    
    long countByEntityIdAndEntityType(Long entityId, EntityType entityType);
} 