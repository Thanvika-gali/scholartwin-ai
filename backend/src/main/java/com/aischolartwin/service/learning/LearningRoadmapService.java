package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.RoadmapDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.learning.LearningRoadmapRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import com.aischolartwin.repository.learning.RoadmapPhaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LearningRoadmapService {

    private final LearningRoadmapRepository roadmapRepository;
    private final RoadmapPhaseRepository phaseRepository;
    private final LearningTaskRepository taskRepository;

    public LearningRoadmapService(
            LearningRoadmapRepository roadmapRepository,
            RoadmapPhaseRepository phaseRepository,
            LearningTaskRepository taskRepository
    ) {
        this.roadmapRepository = roadmapRepository;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<RoadmapDtos.LearningRoadmapResponse> getRoadmaps(User user) {
        List<LearningRoadmap> roadmaps = roadmapRepository.findByUserOrderByCreatedAtDesc(user);
        return roadmaps.stream().map(RoadmapDtos.LearningRoadmapResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public RoadmapDtos.LearningRoadmapResponse getRoadmapDetail(User user, Long id) {
        LearningRoadmap roadmap = roadmapRepository.findDetailedByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + id));
        return buildDetailedResponse(roadmap);
    }

    @Transactional
    public RoadmapDtos.LearningRoadmapResponse createOrGenerateRoadmap(User user, RoadmapDtos.CreateRoadmapRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidPayloadException("Roadmap title is required.");
        }

        LearningRoadmap roadmap = new LearningRoadmap(
                user,
                request.title().trim(),
                request.description(),
                request.targetRole() != null ? request.targetRole() : "Software Development Engineer",
                request.goalType() != null ? request.goalType() : "ROLE",
                request.targetSkillOrTopic()
        );

        if (request.targetCompletionDate() != null) {
            roadmap.setTargetCompletionDate(request.targetCompletionDate());
        } else {
            roadmap.setTargetCompletionDate(LocalDate.now().plusMonths(3));
        }

        LearningRoadmap saved = roadmapRepository.save(roadmap);

        // Populate curriculum structure based on goalType or targetRole
        generateCurriculum(saved, user, request.targetRole(), request.goalType(), request.targetSkillOrTopic());

        recalculateRoadmapProgress(saved);
        LearningRoadmap refreshed = roadmapRepository.findDetailedByIdAndUser(saved.getId(), user).orElse(saved);
        return buildDetailedResponse(refreshed);
    }

    private RoadmapDtos.LearningRoadmapResponse buildDetailedResponse(LearningRoadmap roadmap) {
        List<RoadmapPhase> phases = phaseRepository.findByRoadmapOrderByPhaseOrderAsc(roadmap);
        List<RoadmapDtos.RoadmapPhaseResponse> phaseResponses = new ArrayList<>();
        for (RoadmapPhase phase : phases) {
            List<LearningTask> tasks = taskRepository.findByPhaseOrderByTaskOrderAsc(phase);
            List<RoadmapDtos.LearningTaskResponse> taskResponses = tasks.stream()
                    .map(RoadmapDtos.LearningTaskResponse::fromEntity)
                    .toList();
            phaseResponses.add(new RoadmapDtos.RoadmapPhaseResponse(
                    phase.getId(),
                    phase.getPhaseNumber(),
                    phase.getTitle(),
                    phase.getDescription(),
                    phase.getPhaseOrder(),
                    taskResponses
            ));
        }

        return new RoadmapDtos.LearningRoadmapResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                roadmap.getTargetRole(),
                roadmap.getGoalType(),
                roadmap.getTargetSkillOrTopic(),
                roadmap.getStatus(),
                roadmap.getTargetCompletionDate(),
                roadmap.getEstimatedHours(),
                roadmap.getCompletedHours(),
                roadmap.getCompletionPercentage(),
                phaseResponses,
                roadmap.getCreatedAt()
        );
    }

    @Transactional
    public RoadmapDtos.LearningRoadmapResponse updateRoadmap(User user, Long id, RoadmapDtos.UpdateRoadmapRequest request) {
        LearningRoadmap roadmap = roadmapRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + id));

        if (request.title() != null && !request.title().isBlank()) {
            roadmap.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            roadmap.setDescription(request.description());
        }
        if (request.status() != null) {
            roadmap.setStatus(request.status());
        }
        if (request.targetCompletionDate() != null) {
            roadmap.setTargetCompletionDate(request.targetCompletionDate());
        }

        recalculateRoadmapProgress(roadmap);
        LearningRoadmap saved = roadmapRepository.save(roadmap);
        return RoadmapDtos.LearningRoadmapResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteRoadmap(User user, Long id) {
        LearningRoadmap roadmap = roadmapRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + id));
        roadmapRepository.delete(roadmap);
    }

    @Transactional
    public RoadmapDtos.LearningTaskResponse addTaskToPhase(User user, Long phaseId, RoadmapDtos.CreateTaskRequest request) {
        RoadmapPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phase not found with ID: " + phaseId));

        if (!phase.getRoadmap().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Access denied to phase");
        }

        int nextOrder = phase.getTasks().size() + 1;
        LearningTask task = new LearningTask(
                phase,
                user,
                request.title(),
                request.topic(),
                request.subtopics(),
                nextOrder,
                request.estimatedMinutes() != null ? request.estimatedMinutes() : 60,
                request.learningObjectives(),
                request.practicalExercises(),
                request.completionCriteria()
        );
        task.setMiniProject(request.miniProject());
        task.setPrerequisiteTaskId(request.prerequisiteTaskId());
        task.setPrerequisiteTitle(request.prerequisiteTitle());

        LearningTask saved = taskRepository.save(task);
        recalculateRoadmapProgress(phase.getRoadmap());
        return RoadmapDtos.LearningTaskResponse.fromEntity(saved);
    }

    @Transactional
    public RoadmapDtos.LearningTaskResponse updateTaskStatus(User user, Long taskId, RoadmapDtos.UpdateTaskStatusRequest request) {
        LearningTask task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (request.status() != null) {
            task.setStatus(request.status());
            if (request.status() == TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
                if (request.actualMinutes() != null && request.actualMinutes() > 0) {
                    task.setActualMinutes(request.actualMinutes());
                } else if (task.getActualMinutes() == 0) {
                    task.setActualMinutes(task.getEstimatedMinutes());
                }
            } else {
                task.setCompletedAt(null);
            }
        }

        LearningTask saved = taskRepository.save(task);
        if (task.getPhase() != null && task.getPhase().getRoadmap() != null) {
            recalculateRoadmapProgress(task.getPhase().getRoadmap());
        }
        return RoadmapDtos.LearningTaskResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteTask(User user, Long taskId) {
        LearningTask task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        LearningRoadmap roadmap = task.getPhase() != null ? task.getPhase().getRoadmap() : null;
        taskRepository.delete(task);
        if (roadmap != null) {
            recalculateRoadmapProgress(roadmap);
        }
    }

    public void recalculateRoadmapProgress(LearningRoadmap roadmap) {
        List<RoadmapPhase> phases = phaseRepository.findByRoadmapOrderByPhaseOrderAsc(roadmap);
        int totalTasks = 0;
        int completedTasks = 0;
        int totalEstMinutes = 0;
        int totalActMinutes = 0;

        for (RoadmapPhase phase : phases) {
            List<LearningTask> tasks = taskRepository.findByPhaseOrderByTaskOrderAsc(phase);
            for (LearningTask task : tasks) {
                totalTasks++;
                totalEstMinutes += task.getEstimatedMinutes();
                if (task.getStatus() == TaskStatus.COMPLETED) {
                    completedTasks++;
                    totalActMinutes += (task.getActualMinutes() > 0 ? task.getActualMinutes() : task.getEstimatedMinutes());
                }
            }
        }

        roadmap.setEstimatedHours(Math.round((totalEstMinutes / 60.0) * 10.0) / 10.0);
        roadmap.setCompletedHours(Math.round((totalActMinutes / 60.0) * 10.0) / 10.0);
        int pct = totalTasks > 0 ? (int) Math.round(((double) completedTasks / totalTasks) * 100) : 0;
        roadmap.setCompletionPercentage(pct);
        if (pct >= 100 && totalTasks > 0) {
            roadmap.setStatus(RoadmapStatus.COMPLETED);
        }
        roadmapRepository.save(roadmap);
    }

    private void generateCurriculum(LearningRoadmap roadmap, User user, String targetRole, String goalType, String skillOrTopic) {
        String roleOrSkill = (skillOrTopic != null && !skillOrTopic.isBlank())
                ? skillOrTopic.toLowerCase()
                : (targetRole != null ? targetRole.toLowerCase() : "sde");

        if (roleOrSkill.contains("full") || roleOrSkill.contains("web")) {
            buildFullStackCurriculum(roadmap, user);
        } else if (roleOrSkill.contains("ai") || roleOrSkill.contains("machine learning") || roleOrSkill.contains("data")) {
            buildAiMlCurriculum(roadmap, user);
        } else if (roleOrSkill.contains("dsa") || roleOrSkill.contains("algorithm") || roleOrSkill.contains("interview")) {
            buildDsaInterviewCurriculum(roadmap, user);
        } else {
            // Default SDE / Backend Engineering curriculum
            buildSdeCurriculum(roadmap, user);
        }
    }

    private void buildSdeCurriculum(LearningRoadmap roadmap, User user) {
        // Phase 1: Core Foundations
        RoadmapPhase p1 = new RoadmapPhase(roadmap, 1, "Core Computer Science & Language Mastery", "Master core programming, memory model, and object-oriented paradigms.", 1);
        p1 = phaseRepository.save(p1);

        LearningTask t1_1 = new LearningTask(p1, user, "Java Core & OOP Deep Dive", "Java & OOP",
                "Classes, Inheritance, Polymorphism, Abstract classes, Interfaces, Memory Stack vs Heap",
                1, 90, "Grasp memory allocation, value vs reference passing, and interface design principles.",
                "Implement a flexible in-memory LRU Cache using DoublyLinkedList and HashMap.",
                "All unit tests pass; explain time and space complexity.");
        taskRepository.save(t1_1);

        LearningTask t1_2 = new LearningTask(p1, user, "Java Collections Framework & Generics", "Java Collections",
                "List, Set, Map hierarchies, ArrayList vs LinkedList, HashMap internals, ConcurrentHashMap",
                2, 75, "Understand internal hashing buckets, collision resolution, and thread-safe collections.",
                "Simulate hash bucket collision handling under custom hashCode() implementations.",
                "Demonstrate O(1) average lookup vs O(n) worst case.");
        taskRepository.save(t1_2);

        // Phase 2: Algorithmic Problem Solving
        RoadmapPhase p2 = new RoadmapPhase(roadmap, 2, "Data Structures & Algorithmic Patterns", "Systematic mastery of common interview patterns and data structures.", 2);
        p2 = phaseRepository.save(p2);

        LearningTask t2_1 = new LearningTask(p2, user, "Arrays, Strings & Two Pointers", "Two Pointers",
                "Sorted array two-sum, Container with Most Water, 3Sum, In-place string reversal",
                1, 120, "Recognize two-pointer convergence patterns to reduce O(N^2) brute-force to O(N).",
                "Solve 4 LeetCode medium two-pointer problems on DSA Tracker.",
                "Submit optimal O(1) space solutions.");
        taskRepository.save(t2_1);

        LearningTask t2_2 = new LearningTask(p2, user, "Trees, BST & DFS/BFS Traversal", "Trees & Graphs",
                "Tree traversals (in/pre/post/level), LCA, Diameter of Tree, Serialize/Deserialize",
                2, 120, "Master tree recursion patterns and queue-based level order traversals.",
                "Implement iterative and recursive traversals for binary search tree validation.",
                "Correctly identify boundary edge cases and recursion depths.");
        taskRepository.save(t2_2);

        // Phase 3: Backend & System Design
        RoadmapPhase p3 = new RoadmapPhase(roadmap, 3, "Production Backend Engineering & System Architecture", "Architect scalable REST services, SQL databases, and caching layers.", 3);
        p3 = phaseRepository.save(p3);

        LearningTask t3_1 = new LearningTask(p3, user, "Spring Boot & REST API Architecture", "Spring Boot",
                "Dependency Injection, Controller-Service-Repository pattern, Exception Handling, Validation",
                1, 150, "Build production-grade RESTful APIs with stateless JWT authentication.",
                "Build a mini REST service with pagination, sorting, and standardized error responses.",
                "Code follows clean architecture with 80%+ test coverage.");
        taskRepository.save(t3_1);

        LearningTask t3_2 = new LearningTask(p3, user, "SQL Query Optimization & Database Indexing", "SQL & Databases",
                "B-Tree indexes, Composite indexes, Explain plan, Join algorithms, ACID guarantees",
                2, 120, "Optimize slow queries using explain plans and composite B-tree indexing.",
                "Benchmark a table with 500,000 rows before and after composite indexing.",
                "Query execution drops from >500ms full table scan to <5ms index seek.");
        taskRepository.save(t3_2);
    }

    private void buildFullStackCurriculum(LearningRoadmap roadmap, User user) {
        RoadmapPhase p1 = new RoadmapPhase(roadmap, 1, "Modern Frontend Architecture", "React, component composition, state management, and modern CSS.", 1);
        p1 = phaseRepository.save(p1);

        LearningTask t1 = new LearningTask(p1, user, "React Component Architecture & Hooks", "React",
                "useState, useEffect, useMemo, useCallback, Custom Hooks, Context API",
                1, 120, "Understand React 19 rendering cycle, closure traps, and memoization.",
                "Build an interactive dashboard component with theme toggles and state lifting.",
                "Clean code with zero memory leaks and proper cleanup functions.");
        taskRepository.save(t1);

        RoadmapPhase p2 = new RoadmapPhase(roadmap, 2, "Backend Services & API Integration", "RESTful services, authentication, and database persistence.", 2);
        p2 = phaseRepository.save(p2);

        LearningTask t2 = new LearningTask(p2, user, "JWT Authentication & Secure APIs", "Security & Auth",
                "Stateless JWT, Refresh tokens, BCrypt hashing, CORS, Role-based authorization",
                1, 150, "Implement secure authentication workflows with token verification.",
                "Build login, registration, and protected routes with Axios/Fetch interceptors.",
                "Verify unauthorized access is properly blocked with HTTP 401/403.");
        taskRepository.save(t2);
    }

    private void buildDsaInterviewCurriculum(LearningRoadmap roadmap, User user) {
        RoadmapPhase p1 = new RoadmapPhase(roadmap, 1, "Linear Patterns & Hash Tables", "Master arrays, hash maps, two pointers, and sliding window.", 1);
        p1 = phaseRepository.save(p1);

        LearningTask t1 = new LearningTask(p1, user, "Sliding Window & Hash Maps", "Sliding Window",
                "Fixed size vs dynamic size window, Longest Substring Without Repeating Characters",
                1, 120, "Master O(N) sliding window invariants.",
                "Solve 5 sliding window problems on DSA Tracker.",
                "All problems solved with optimal time and space complexity.");
        taskRepository.save(t1);

        RoadmapPhase p2 = new RoadmapPhase(roadmap, 2, "Dynamic Programming & Recursion", "Top-down memoization, bottom-up tabulation, 1D and 2D DP.", 2);
        p2 = phaseRepository.save(p2);

        LearningTask t2 = new LearningTask(p2, user, "1D & 2D Dynamic Programming Patterns", "Dynamic Programming",
                "Climbing stairs, House Robber, Coin Change, Longest Common Subsequence",
                1, 180, "Identify state transitions and base cases.",
                "Solve 6 classical DP problems and explain state transition equations.",
                "Verify O(N) or O(N*M) optimal space optimizations.");
        taskRepository.save(t2);
    }

    private void buildAiMlCurriculum(LearningRoadmap roadmap, User user) {
        RoadmapPhase p1 = new RoadmapPhase(roadmap, 1, "Python & Mathematical Foundations", "Data processing with NumPy, Pandas, and foundational linear algebra.", 1);
        p1 = phaseRepository.save(p1);

        LearningTask t1 = new LearningTask(p1, user, "Data Manipulation & Feature Engineering", "Python & Pandas",
                "Vectorized operations, handling missing data, one-hot encoding, feature scaling",
                1, 120, "Prepare clean datasets for model training.",
                "Clean and transform a real-world dataset for model pipeline ingestion.",
                "Zero data leakage in train/test splits.");
        taskRepository.save(t1);

        RoadmapPhase p2 = new RoadmapPhase(roadmap, 2, "Deep Learning & LLM Integration", "Neural networks, embeddings, vector databases, and RAG pipelines.", 2);
        p2 = phaseRepository.save(p2);

        LearningTask t2 = new LearningTask(p2, user, "Vector Embeddings & Retrieval-Augmented Generation", "LLMs & RAG",
                "Dense embeddings, cosine similarity, vector indexing, prompt engineering",
                1, 180, "Understand semantic search and grounding generation with context chunks.",
                "Build a local document search pipeline using vector embeddings.",
                "Accurately cite source documents in answer generation.");
        taskRepository.save(t2);
    }
}
