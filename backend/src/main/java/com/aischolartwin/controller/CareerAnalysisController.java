package com.aischolartwin.controller;

import com.aischolartwin.dto.ResumeAnalysisResponse;
import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.ResumeAnalysis;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.CareerActionService;
import com.aischolartwin.service.CareerSkillService;
import com.aischolartwin.service.ResumeAnalysisService;
import com.aischolartwin.service.ResumeService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career")
public class CareerAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;
    private final ResumeService resumeService;
    private final CareerSkillService careerSkillService;
    private final CareerActionService careerActionService;
    private final UserService userService;

    public CareerAnalysisController(ResumeAnalysisService resumeAnalysisService,
                                    ResumeService resumeService,
                                    CareerSkillService careerSkillService,
                                    CareerActionService careerActionService,
                                    UserService userService) {
        this.resumeAnalysisService = resumeAnalysisService;
        this.resumeService = resumeService;
        this.careerSkillService = careerSkillService;
        this.careerActionService = careerActionService;
        this.userService = userService;
    }

    @PostMapping("/resumes/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> reanalyzeResume(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        Resume resume = resumeService.getResumeEntity(user, id);

        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(resume);
        careerSkillService.importSkillsFromResume(user, resume.getId());
        careerActionService.generateRecommendedActionsFromAnalysis(user, analysis);

        return ResponseEntity.ok(resumeAnalysisService.getAnalysisResponse(analysis));
    }

    @GetMapping("/resumes/{id}/analysis")
    public ResponseEntity<ResumeAnalysisResponse> getLatestResumeAnalysis(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResumeAnalysisResponse response = resumeAnalysisService.getLatestAnalysisForResume(user, id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<ResumeAnalysisResponse> getAnalysisById(
            Authentication authentication,
            @PathVariable Long analysisId
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResumeAnalysisResponse response = resumeAnalysisService.getAnalysisById(user, analysisId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/history")
    public ResponseEntity<List<ResumeAnalysisResponse>> getAnalysisHistory(
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<ResumeAnalysisResponse> history = resumeAnalysisService.getAnalysisHistory(user);
        return ResponseEntity.ok(history);
    }
}

