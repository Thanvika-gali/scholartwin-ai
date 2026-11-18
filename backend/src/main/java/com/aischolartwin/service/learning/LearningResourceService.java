package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.LearningResourceDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningResourceAssociation;
import com.aischolartwin.entity.learning.LearningResourceType;
import com.aischolartwin.entity.learning.LearningTask;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.learning.LearningResourceAssociationRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class LearningResourceService {

    private final LearningResourceAssociationRepository associationRepository;
    private final LearningTaskRepository taskRepository;

    public LearningResourceService(
            LearningResourceAssociationRepository associationRepository,
            LearningTaskRepository taskRepository
    ) {
        this.associationRepository = associationRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<LearningResourceDtos.LearningResourceResponse> getResources(User user, String topic, Long taskId) {
        if (taskId != null) {
            LearningTask task = taskRepository.findByIdAndUser(taskId, user).orElse(null);
            if (task != null) {
                return associationRepository.findByUserAndTask(user, task)
                        .stream().map(LearningResourceDtos.LearningResourceResponse::fromEntity).toList();
            }
        }
        if (topic != null && !topic.isBlank()) {
            return associationRepository.findByUserAndTopicIgnoreCase(user, topic.trim())
                    .stream().map(LearningResourceDtos.LearningResourceResponse::fromEntity).toList();
        }
        return associationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(LearningResourceDtos.LearningResourceResponse::fromEntity).toList();
    }

    @Transactional
    public LearningResourceDtos.LearningResourceResponse associateResource(User user, LearningResourceDtos.AssociateResourceRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidPayloadException("Resource title is required.");
        }

        LearningTask task = null;
        if (request.taskId() != null) {
            task = taskRepository.findByIdAndUser(request.taskId(), user).orElse(null);
        }

        LearningResourceAssociation assoc = new LearningResourceAssociation(
                user,
                task,
                request.topic() != null ? request.topic() : (task != null ? task.getTopic() : "General"),
                request.resourceType() != null ? request.resourceType() : LearningResourceType.EXTERNAL_URL,
                request.resourceId(),
                request.title().trim(),
                request.url(),
                request.notes()
        );

        LearningResourceAssociation saved = associationRepository.save(assoc);
        return LearningResourceDtos.LearningResourceResponse.fromEntity(saved);
    }

    @Transactional
    public LearningResourceDtos.LearningResourceResponse markStudied(User user, Long resourceId, boolean studied) {
        LearningResourceAssociation assoc = associationRepository.findByIdAndUser(resourceId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resource association not found with ID: " + resourceId));

        assoc.setStudied(studied);
        assoc.setStudiedAt(studied ? Instant.now() : null);

        LearningResourceAssociation saved = associationRepository.save(assoc);
        return LearningResourceDtos.LearningResourceResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteResource(User user, Long resourceId) {
        LearningResourceAssociation assoc = associationRepository.findByIdAndUser(resourceId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resource association not found with ID: " + resourceId));
        associationRepository.delete(assoc);
    }
}
