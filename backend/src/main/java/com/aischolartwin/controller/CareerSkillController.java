package com.aischolartwin.controller;

import com.aischolartwin.dto.CareerSkillRequest;
import com.aischolartwin.dto.CareerSkillResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.CareerSkillService;
import com.aischolartwin.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career/skills")
public class CareerSkillController {

    private final CareerSkillService careerSkillService;
    private final UserService userService;

    public CareerSkillController(CareerSkillService careerSkillService, UserService userService) {
        this.careerSkillService = careerSkillService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CareerSkillResponse>> getSkills(
            Authentication authentication,
            @RequestParam(value = "category", required = false) String category
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<CareerSkillResponse> skills = careerSkillService.getSkills(user, category);
        return ResponseEntity.ok(skills);
    }

    @PostMapping
    public ResponseEntity<CareerSkillResponse> addSkill(
            Authentication authentication,
            @RequestBody CareerSkillRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CareerSkillResponse created = careerSkillService.addSkill(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareerSkillResponse> updateSkill(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CareerSkillRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CareerSkillResponse updated = careerSkillService.updateSkill(user, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        careerSkillService.deleteSkill(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/import/{resumeId}", "/import-from-resume/{resumeId}"})
    public ResponseEntity<List<CareerSkillResponse>> importFromResume(
            Authentication authentication,
            @PathVariable Long resumeId
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<CareerSkillResponse> imported = careerSkillService.importSkillsFromResume(user, resumeId);
        return ResponseEntity.ok(imported);
    }
}

