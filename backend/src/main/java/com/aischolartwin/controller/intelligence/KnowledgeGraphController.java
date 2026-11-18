package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.KnowledgeGraphDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.KnowledgeGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intelligence/knowledge-graph")
public class KnowledgeGraphController {

    private final KnowledgeGraphService graphService;
    private final UserService userService;

    public KnowledgeGraphController(KnowledgeGraphService graphService, UserService userService) {
        this.graphService = graphService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<KnowledgeGraphDtos.KnowledgeGraphResponse> getGraph(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(graphService.getGraph(user, q, category));
    }

    @GetMapping("/nodes/{id:\\d+}")
    public ResponseEntity<KnowledgeGraphDtos.GraphNodeDto> getNodeDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(graphService.getNodeDetail(user, id));
    }

    @PostMapping("/sync")
    public ResponseEntity<KnowledgeGraphDtos.KnowledgeGraphResponse> syncGraph(
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(graphService.syncGraph(user));
    }
}
