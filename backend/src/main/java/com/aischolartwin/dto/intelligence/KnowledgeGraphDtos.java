package com.aischolartwin.dto.intelligence;

import com.aischolartwin.entity.intelligence.KnowledgeNode;
import com.aischolartwin.entity.intelligence.KnowledgeRelationship;
import com.aischolartwin.entity.intelligence.NodeType;
import com.aischolartwin.entity.intelligence.RelationType;

import java.util.List;

public class KnowledgeGraphDtos {

    public record GraphNodeDto(
            Long id,
            String name,
            String label,
            NodeType nodeType,
            String description,
            String category,
            String masteryLevel,
            Long referenceId,
            String referenceType
    ) {
        public static GraphNodeDto fromEntity(KnowledgeNode n) {
            return new GraphNodeDto(
                    n.getId(),
                    n.getName(),
                    n.getName(),
                    n.getNodeType(),
                    n.getDescription(),
                    n.getCategory(),
                    n.getMasteryLevel(),
                    n.getReferenceId(),
                    n.getReferenceType()
            );
        }
    }

    public record GraphEdgeDto(
            Long id,
            Long sourceId,
            Long targetId,
            String sourceName,
            String targetName,
            RelationType relationType,
            String description,
            Double strength,
            String evidenceSource
    ) {
        public static GraphEdgeDto fromEntity(KnowledgeRelationship r) {
            return new GraphEdgeDto(
                    r.getId(),
                    r.getSourceNode().getId(),
                    r.getTargetNode().getId(),
                    r.getSourceNode().getName(),
                    r.getTargetNode().getName(),
                    r.getRelationType(),
                    r.getDescription(),
                    r.getStrength(),
                    r.getEvidenceSource()
            );
        }
    }

    public record KnowledgeGraphResponse(
            int nodesCount,
            int edgesCount,
            List<GraphNodeDto> nodes,
            List<GraphEdgeDto> edges
    ) {}
}
