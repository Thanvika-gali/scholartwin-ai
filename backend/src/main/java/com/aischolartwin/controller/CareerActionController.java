package com.aischolartwin.controller;

import com.aischolartwin.dto.CareerActionRequest;
import com.aischolartwin.dto.CareerActionResponse;
import com.aischolartwin.dto.CareerActionUpdateRequest;
import com.aischolartwin.entity.CareerActionStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.CareerActionService;
import com.aischolartwin.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career/actions")
public class CareerActionController {

    private final CareerActionService careerActionService;
    private final UserService userService;

    public CareerActionController(CareerActionService careerActionService, UserService userService) {
        this.careerActionService = careerActionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CareerActionResponse>> getActions(
            Authentication authentication,
            @RequestParam(value = "status", required = false) CareerActionStatus status
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<CareerActionResponse> actions = careerActionService.getActions(user, status);
        return ResponseEntity.ok(actions);
    }

    @PostMapping
    public ResponseEntity<CareerActionResponse> createAction(
            Authentication authentication,
            @RequestBody CareerActionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CareerActionResponse created = careerActionService.createAction(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareerActionResponse> updateAction(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CareerActionUpdateRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CareerActionResponse updated = careerActionService.updateAction(user, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAction(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        careerActionService.deleteAction(user, id);
        return ResponseEntity.noContent().build();
    }
}

