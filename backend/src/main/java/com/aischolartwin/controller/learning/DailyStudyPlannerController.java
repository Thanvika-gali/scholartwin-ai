package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.PlannerDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.DailyStudyPlannerService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/learning/planner")
public class DailyStudyPlannerController {

    private final DailyStudyPlannerService plannerService;
    private final UserService userService;

    public DailyStudyPlannerController(DailyStudyPlannerService plannerService, UserService userService) {
        this.plannerService = plannerService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PlannerDtos.DailyPlanSummaryResponse> getDailyPlan(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(plannerService.getDailyPlan(user, date));
    }

    @PostMapping("/tasks")
    public ResponseEntity<PlannerDtos.PlannerTaskResponse> createPlannerTask(
            Authentication authentication,
            @Valid @RequestBody PlannerDtos.CreatePlannerTaskRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        PlannerDtos.PlannerTaskResponse response = plannerService.createPlannerTask(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<PlannerDtos.PlannerTaskResponse> updatePlannerTask(
            Authentication authentication,
            @PathVariable Long taskId,
            @Valid @RequestBody PlannerDtos.UpdatePlannerTaskRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(plannerService.updatePlannerTask(user, taskId, request));
    }

    @PostMapping("/tasks/{taskId}/reschedule")
    public ResponseEntity<PlannerDtos.PlannerTaskResponse> rescheduleTask(
            Authentication authentication,
            @PathVariable Long taskId,
            @Valid @RequestBody PlannerDtos.RescheduleTaskRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(plannerService.rescheduleTask(user, taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deletePlannerTask(
            Authentication authentication,
            @PathVariable Long taskId
    ) {
        User user = userService.getByEmail(authentication.getName());
        plannerService.deletePlannerTask(user, taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auto-populate")
    public ResponseEntity<PlannerDtos.DailyPlanSummaryResponse> autoPopulateTasks(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(plannerService.autoPopulateTodayTasks(user, date));
    }
}
