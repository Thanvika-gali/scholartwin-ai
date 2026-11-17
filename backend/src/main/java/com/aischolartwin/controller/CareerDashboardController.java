package com.aischolartwin.controller;

import com.aischolartwin.dto.CareerDashboardSummaryResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.CareerDashboardService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/career/dashboard")
public class CareerDashboardController {

    private final CareerDashboardService careerDashboardService;
    private final UserService userService;

    public CareerDashboardController(CareerDashboardService careerDashboardService, UserService userService) {
        this.careerDashboardService = careerDashboardService;
        this.userService = userService;
    }

    @GetMapping("/summary")
    public ResponseEntity<CareerDashboardSummaryResponse> getSummary(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        CareerDashboardSummaryResponse summary = careerDashboardService.getDashboardSummary(user);
        return ResponseEntity.ok(summary);
    }
}

