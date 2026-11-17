package com.aischolartwin.service;

import com.aischolartwin.dto.KnowledgeSearchResponse;
import com.aischolartwin.dto.ResourceItemResponse;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeSearchServiceTest {

    @Mock
    private KnowledgeResourceService resourceService;

    @InjectMocks
    private KnowledgeSearchService searchService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Jane", "jane@example.com", "pass");
        user.setId(1L);
    }

    @Test
    void search_ReturnsResultsWithFacetsAndReportsSemanticSearchUnavailable() {
        ResourceItemResponse docItem = new ResourceItemResponse(
                ResourceType.DOCUMENT, 1L, "B-Trees in DBMS.pdf", "Lecture notes on B-Trees",
                null, null, "Computer Science", 5L, "DBMS", "Academic Library / DBMS",
                "pdf", "1.2 MB", 1024L, 12, "PROCESSED",
                false, false, false, Collections.emptyList(), "Extracted summary", 1,
                Instant.now(), Instant.now(), Instant.now()
        );

        ResourceItemResponse noteItem = new ResourceItemResponse(
                ResourceType.NOTE, 2L, "B-Tree vs B+Tree", "Quick revision",
                "B+ trees have data in leaves only", null, "Computer Science", 5L, "DBMS", "Academic Library / DBMS",
                null, null, 0L, null, null,
                true, false, false, Collections.emptyList(), null, 1,
                Instant.now(), Instant.now(), Instant.now()
        );

        when(resourceService.listResources(eq(user), isNull(), isNull(), eq(false), isNull(), eq(false), isNull(), eq("B-Tree"), anyString()))
                .thenReturn(List.of(docItem, noteItem));

        KnowledgeSearchResponse response = searchService.search(user, "B-Tree", null, null, null, null, "RECENT_MODIFIED");

        assertNotNull(response);
        assertEquals(2, response.totalCount());
        assertFalse(response.semanticSearchAvailable(), "Must report semanticSearchAvailable as false");
        assertEquals("B-Tree", response.query());

        assertEquals(2L, response.typeFacets().get("ALL"));
        assertEquals(1L, response.typeFacets().get("DOCUMENT"));
        assertEquals(1L, response.typeFacets().get("NOTE"));
        assertEquals(0L, response.typeFacets().get("LINK"));
    }
}
