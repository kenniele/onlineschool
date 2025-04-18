package com.onlineSchool.service;

import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import com.onlineSchool.model.User;
import com.onlineSchool.repository.LikeRepository;
import com.onlineSchool.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    public List<Like> getLikesByEntity(EntityType entityType, Long entityId) {
        return likeRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<Like> getLikesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return likeRepository.findByUserId(userId);
    }

    public Like like(Long userId, Long entityId, EntityType entityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (hasLiked(userId, entityId, entityType)) {
            throw new IllegalStateException("User has already liked this entity");
        }

        Like like = new Like();
        like.setUser(user);
        like.setEntityId(entityId);
        like.setEntityType(entityType);

        return likeRepository.save(like);
    }

    public void unlike(Long userId, Long entityId, EntityType entityType) {
        Optional<Like> existingLike = likeRepository.findByUserIdAndEntityIdAndEntityType(userId, entityId, entityType);
        existingLike.ifPresent(likeRepository::delete);
    }

    public Like createLike(Like like) {
        if (!userRepository.existsById(like.getUser().getId())) {
            throw new EntityNotFoundException("User not found with id: " + like.getUser().getId());
        }
        
        if (hasLiked(like.getUser().getId(), like.getEntityId(), like.getEntityType())) {
            throw new IllegalStateException("User has already liked this entity");
        }
        
        return likeRepository.save(like);
    }

    public void deleteLike(Long id) {
        if (!likeRepository.existsById(id)) {
            throw new EntityNotFoundException("Like not found with id: " + id);
        }
        likeRepository.deleteById(id);
    }

    public Like getLikeById(Long id) {
        return likeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Like not found with id: " + id));
    }

    public boolean isLikeOwner(Long likeId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return likeRepository.existsByIdAndUserId(likeId, userId);
    }

    public List<Like> getLikesByUserAndEntity(Long userId, EntityType entityType, Long entityId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, entityType, entityId);
    }

    public long countLikes(Long entityId, EntityType entityType) {
        return likeRepository.countByEntityIdAndEntityType(entityId, entityType);
    }

    public boolean hasLiked(Long userId, Long entityId, EntityType entityType) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return likeRepository.findByUserIdAndEntityIdAndEntityType(userId, entityId, entityType).isPresent();
    }

    public boolean isUserLikedEntity(Long userId, EntityType entityType, Long entityId) {
        return !getLikesByUserAndEntity(userId, entityType, entityId).isEmpty();
    }
} 