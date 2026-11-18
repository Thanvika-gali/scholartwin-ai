package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.KnowledgeNode;
import com.aischolartwin.entity.intelligence.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, Long> {

    List<KnowledgeNode> findByUserOrderByCategoryAscNameAsc(User user);

    List<KnowledgeNode> findByUserAndNodeTypeOrderByNameAsc(User user, NodeType nodeType);

    Optional<KnowledgeNode> findByIdAndUser(Long id, User user);

    Optional<KnowledgeNode> findByNameIgnoreCaseAndUser(String name, User user);

    @Query("SELECT n FROM KnowledgeNode n WHERE n.user = :user AND " +
            "(LOWER(n.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(n.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(n.category) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY n.name ASC")
    List<KnowledgeNode> searchNodes(@Param("user") User user, @Param("query") String query);

    void deleteByUser(User user);
}
