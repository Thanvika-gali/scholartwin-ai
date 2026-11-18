package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.KnowledgeNode;
import com.aischolartwin.entity.intelligence.KnowledgeRelationship;
import com.aischolartwin.entity.intelligence.RelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeRelationshipRepository extends JpaRepository<KnowledgeRelationship, Long> {

    List<KnowledgeRelationship> findByUser(User user);

    List<KnowledgeRelationship> findBySourceNodeOrTargetNode(KnowledgeNode sourceNode, KnowledgeNode targetNode);

    boolean existsByUserAndSourceNodeAndTargetNodeAndRelationType(User user,
                                                                 KnowledgeNode sourceNode,
                                                                 KnowledgeNode targetNode,
                                                                 RelationType relationType);

    void deleteByUser(User user);
}
