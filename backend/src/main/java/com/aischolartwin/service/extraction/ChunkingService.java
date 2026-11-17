package com.aischolartwin.service.extraction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private final int defaultMaxChunkSize;
    private final int defaultOverlap;

    public ChunkingService(
            @Value("${app.document.chunk-max-size:1000}") int defaultMaxChunkSize,
            @Value("${app.document.chunk-overlap:150}") int defaultOverlap
    ) {
        this.defaultMaxChunkSize = defaultMaxChunkSize;
        this.defaultOverlap = defaultOverlap;
    }

    public List<GeneratedChunk> chunkPages(List<ExtractedPage> pages) {
        return chunkPages(pages, defaultMaxChunkSize, defaultOverlap);
    }

    public List<GeneratedChunk> chunkPages(List<ExtractedPage> pages, int maxChunkSize, int overlap) {
        List<GeneratedChunk> chunks = new ArrayList<>();
        if (pages == null || pages.isEmpty()) {
            return chunks;
        }

        // Build list of text segments tagged with their source page number
        List<TextSegment> segments = new ArrayList<>();
        for (ExtractedPage page : pages) {
            String text = page.getText();
            if (text == null || text.isBlank()) {
                continue;
            }

            // Split into paragraphs first
            String[] paragraphs = text.split("\n\n");
            for (String p : paragraphs) {
                String trimmedP = p.trim();
                if (trimmedP.isEmpty()) {
                    continue;
                }

                if (trimmedP.length() <= maxChunkSize) {
                    segments.add(new TextSegment(trimmedP, page.getPageNumber()));
                } else {
                    // Paragraph exceeds maxChunkSize: split into sentence/clause units
                    List<String> sentences = splitIntoSentences(trimmedP);
                    for (String sentence : sentences) {
                        String trimmedSentence = sentence.trim();
                        if (!trimmedSentence.isEmpty()) {
                            segments.add(new TextSegment(trimmedSentence, page.getPageNumber()));
                        }
                    }
                }
            }
        }

        if (segments.isEmpty()) {
            return chunks;
        }

        // Aggregate segments into chunks with overlap
        StringBuilder currentChunkText = new StringBuilder();
        int chunkIndex = 0;
        int chunkStartPage = segments.get(0).pageNumber;
        int chunkEndPage = segments.get(0).pageNumber;

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);

            // If single segment itself exceeds maxChunkSize, hard chunk it
            if (segment.text.length() > maxChunkSize) {
                if (currentChunkText.length() > 0) {
                    chunks.add(new GeneratedChunk(chunkIndex++, currentChunkText.toString().trim(), chunkStartPage, chunkEndPage));
                    currentChunkText.setLength(0);
                }

                String longText = segment.text;
                int start = 0;
                while (start < longText.length()) {
                    int end = Math.min(start + maxChunkSize, longText.length());
                    String sub = longText.substring(start, end).trim();
                    if (!sub.isEmpty()) {
                        chunks.add(new GeneratedChunk(chunkIndex++, sub, segment.pageNumber, segment.pageNumber));
                    }
                    start += (maxChunkSize - overlap);
                    if (start <= 0 || start >= longText.length()) {
                        break;
                    }
                }

                if (i + 1 < segments.size()) {
                    chunkStartPage = segments.get(i + 1).pageNumber;
                    chunkEndPage = chunkStartPage;
                }
                continue;
            }

            int prospectiveLength = currentChunkText.length() + (currentChunkText.length() > 0 ? 2 : 0) + segment.text.length();

            if (prospectiveLength <= maxChunkSize) {
                if (currentChunkText.length() > 0) {
                    currentChunkText.append("\n\n");
                } else {
                    chunkStartPage = segment.pageNumber;
                }
                currentChunkText.append(segment.text);
                chunkEndPage = segment.pageNumber;
            } else {
                // Finalize current chunk
                String completedText = currentChunkText.toString().trim();
                if (!completedText.isEmpty()) {
                    chunks.add(new GeneratedChunk(chunkIndex++, completedText, chunkStartPage, chunkEndPage));
                }

                // Compute overlap from the end of completed chunk
                String overlapPrefix = extractOverlap(completedText, overlap);
                currentChunkText.setLength(0);
                if (!overlapPrefix.isEmpty()) {
                    currentChunkText.append(overlapPrefix).append("\n\n");
                } else {
                    chunkStartPage = segment.pageNumber;
                }
                currentChunkText.append(segment.text);
                chunkEndPage = segment.pageNumber;
            }
        }

        // Flush any remaining text
        if (currentChunkText.length() > 0) {
            String remaining = currentChunkText.toString().trim();
            if (!remaining.isEmpty()) {
                chunks.add(new GeneratedChunk(chunkIndex, remaining, chunkStartPage, chunkEndPage));
            }
        }

        return chunks;
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        // Split on period, exclamation, question mark followed by space or newline
        String[] raw = text.split("(?<=[.!?])\\s+");
        for (String s : raw) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
            }
        }
        return sentences;
    }

    private String extractOverlap(String text, int overlapSize) {
        if (text == null || text.length() <= overlapSize) {
            return text != null ? text : "";
        }
        int startIndex = text.length() - overlapSize;
        // Advance to next whitespace to avoid chopping a word in half
        int spaceIndex = text.indexOf(' ', startIndex);
        if (spaceIndex != -1 && spaceIndex < text.length() - 1) {
            return text.substring(spaceIndex + 1).trim();
        }
        return text.substring(startIndex).trim();
    }

    private static class TextSegment {
        final String text;
        final int pageNumber;

        TextSegment(String text, int pageNumber) {
            this.text = text;
            this.pageNumber = pageNumber;
        }
    }
}

