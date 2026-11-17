package com.aischolartwin.service;

import com.aischolartwin.service.extraction.ChunkingService;
import com.aischolartwin.service.extraction.ExtractedPage;
import com.aischolartwin.service.extraction.GeneratedChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkingServiceTest {

    private ChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        chunkingService = new ChunkingService(1000, 150);
    }

    @Test
    @DisplayName("Return empty list for null or empty pages")
    void shouldReturnEmptyListForEmptyPages() {
        assertThat(chunkingService.chunkPages(null)).isEmpty();
        assertThat(chunkingService.chunkPages(new ArrayList<>())).isEmpty();
    }

    @Test
    @DisplayName("Single small page results in a single chunk with index 0")
    void shouldCreateSingleChunkForSmallDocument() {
        List<ExtractedPage> pages = List.of(
                new ExtractedPage(1, "Artificial Intelligence in Academic Research.\n\nKey methodologies and approaches.", false, false)
        );

        List<GeneratedChunk> chunks = chunkingService.chunkPages(pages);

        assertThat(chunks).hasSize(1);
        GeneratedChunk chunk = chunks.get(0);
        assertThat(chunk.getChunkIndex()).isEqualTo(0);
        assertThat(chunk.getStartPage()).isEqualTo(1);
        assertThat(chunk.getEndPage()).isEqualTo(1);
        assertThat(chunk.getText()).contains("Artificial Intelligence in Academic Research.");
    }

    @Test
    @DisplayName("Multi-page document spans correct page references and sequentially indexed chunks")
    void shouldSpanPageReferencesCorrectly() {
        StringBuilder p1Text = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            p1Text.append("Page 1 Paragraph ").append(i).append(": ").append("Detailed academic notes on linear algebra and vectors. ".repeat(4)).append("\n\n");
        }

        StringBuilder p2Text = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            p2Text.append("Page 2 Paragraph ").append(i).append(": ").append("Eigenvalues, matrix factorizations, and singular value decomposition. ".repeat(4)).append("\n\n");
        }

        List<ExtractedPage> pages = List.of(
                new ExtractedPage(1, p1Text.toString(), false, false),
                new ExtractedPage(2, p2Text.toString(), false, false)
        );

        List<GeneratedChunk> chunks = chunkingService.chunkPages(pages, 500, 50);

        assertThat(chunks).isNotEmpty();
        // Verify sequential 0-based indexing
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).getChunkIndex()).isEqualTo(i);
            assertThat(chunks.get(i).getStartPage()).isGreaterThanOrEqualTo(1);
            assertThat(chunks.get(i).getEndPage()).isGreaterThanOrEqualTo(chunks.get(i).getStartPage());
            assertThat(chunks.get(i).getText()).isNotEmpty();
        }

        // Verify that the first chunk starts on page 1 and last chunk ends on page 2
        assertThat(chunks.get(0).getStartPage()).isEqualTo(1);
        assertThat(chunks.get(chunks.size() - 1).getEndPage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ensure chunk size respects boundary with overlap")
    void shouldRespectMaxChunkSize() {
        int maxChunkSize = 300;
        int overlap = 50;

        List<ExtractedPage> pages = List.of(
                new ExtractedPage(1, "First long academic section with diverse information about graph neural networks. ".repeat(10), false, false)
        );

        List<GeneratedChunk> chunks = chunkingService.chunkPages(pages, maxChunkSize, overlap);

        assertThat(chunks.size()).isGreaterThan(1);
        for (GeneratedChunk chunk : chunks) {
            assertThat(chunk.getText().length()).isLessThanOrEqualTo(maxChunkSize + 100); // Allow slight margin for sentence boundary
        }
    }
}
