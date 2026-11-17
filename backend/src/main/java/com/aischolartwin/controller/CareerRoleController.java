package com.aischolartwin.controller;

import com.aischolartwin.dto.RoleReadinessDto;
import com.aischolartwin.dto.SkillGapAnalysisResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.RoleReadinessService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career")
public class CareerRoleController {

    private final RoleReadinessService roleReadinessService;
    private final UserService userService;

    public CareerRoleController(RoleReadinessService roleReadinessService, UserService userService) {
        this.roleReadinessService = roleReadinessService;
        this.userService = userService;
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleReadinessService.RoleDefinition>> getAvailableRoles() {
        return ResponseEntity.ok(roleReadinessService.getAvailableRoles());
    }

    @GetMapping({"/roles/{roleId}", "/roles/{roleId}/readiness"})
    public ResponseEntity<RoleReadinessDto> getRoleReadiness(
            Authentication authentication,
            @PathVariable String roleId
    ) {
        User user = userService.getByEmail(authentication.getName());
        RoleReadinessDto readiness = roleReadinessService.evaluateRoleReadiness(user, roleId);
        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/skills/gap-analysis")
    public ResponseEntity<SkillGapAnalysisResponse> getSkillGapAnalysis(
            Authentication authentication,
            @RequestParam(value = "targetRole", required = false, defaultValue = "sde") String targetRole
    ) {
        User user = userService.getByEmail(authentication.getName());
        SkillGapAnalysisResponse response = roleReadinessService.getSkillGapAnalysis(user, targetRole);
        return ResponseEntity.ok(response);
    }
}

