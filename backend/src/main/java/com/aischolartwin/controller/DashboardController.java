package com.aischolartwin.controller;

import com.aischolartwin.dto.DashboardSummaryResponse;
import com.aischolartwin.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing personalized summary metrics and recent learning activity for the student dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        DashboardSummaryResponse summary = dashboardService.getSummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }
}

