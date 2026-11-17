package com.aischolartwin.service;

import com.aischolartwin.service.extraction.TextPreprocessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextPreprocessingServiceTest {

    private TextPreprocessingService textPreprocessingService;

    @BeforeEach
    void setUp() {
        textPreprocessingService = new TextPreprocessingService();
    }

    @Test
    @DisplayName("Handle null and blank input gracefully")
    void shouldHandleNullAndBlankInput() {
        assertThat(textPreprocessingService.preprocess(null)).isEmpty();
        assertThat(textPreprocessingService.preprocess("")).isEmpty();
        assertThat(textPreprocessingService.preprocess("    \n\t  ")).isEmpty();
    }

    @Test
    @DisplayName("Normalize excessive spaces and newlines while preserving paragraph separation")
    void shouldNormalizeExcessiveSpacesAndNewlines() {
        String input = "Paragraph 1 with   multiple    spaces.\n\n\n\n\nParagraph 2 after excessive linebreaks.";
        String result = textPreprocessingService.preprocess(input);

        assertThat(result).isEqualTo("Paragraph 1 with multiple spaces.\n\nParagraph 2 after excessive linebreaks.");
    }

    @Test
    @DisplayName("Filter unprintable control characters without damaging valid text")
    void shouldFilterControlCharacters() {
        String input = "Valid text\u0000 with control chars\u0007 and formulas: f(x) = x^2.";
        String result = textPreprocessingService.preprocess(input);

        assertThat(result).isEqualTo("Valid text with control chars and formulas: f(x) = x^2.");
    }

    @Test
    @DisplayName("Normalize Windows CRLF line endings to LF")
    void shouldNormalizeCrlfEndings() {
        String input = "Line one\r\nLine two\r\n\r\nLine three";
        String result = textPreprocessingService.preprocess(input);

        assertThat(result).isEqualTo("Line one\nLine two\n\nLine three");
    }
}

