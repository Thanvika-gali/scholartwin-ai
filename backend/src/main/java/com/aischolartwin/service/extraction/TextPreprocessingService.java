package com.aischolartwin.service.extraction;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TextPreprocessingService {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ \\t\\x0B\\f]+");
    private static final Pattern EXCESSIVE_NEWLINES = Pattern.compile("\\n{3,}");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0E-\\x1F]");

    /**
     * Preprocesses raw extracted text to clean noise while carefully preserving
     * mathematical notation, technical terms, code indentation, and paragraph breaks.
     */
    public String preprocess(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        // 1. Normalize line endings
        String text = rawText.replace("\r\n", "\n").replace('\r', '\n');

        // 2. Remove unprintable control characters (excluding newline and tab)
        text = CONTROL_CHARS.matcher(text).replaceAll("");

        // 3. Normalize horizontal whitespace line-by-line while preserving newlines
        String[] lines = text.split("\n", -1);
        StringBuilder cleaned = new StringBuilder(text.length());
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = MULTIPLE_SPACES.matcher(line).replaceAll(" ").trim();
            cleaned.append(trimmedLine);
            if (i < lines.length - 1) {
                cleaned.append("\n");
            }
        }

        // 4. Reduce excessive blank lines (more than 2 consecutive newlines) to double newlines (paragraphs)
        text = EXCESSIVE_NEWLINES.matcher(cleaned.toString()).replaceAll("\n\n");

        return text.trim();
    }
}

