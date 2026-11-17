package com.aischolartwin.dto;

import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSAStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DSAProblemRequest(
        @NotBlank(message = "Problem title is required.")
        @Size(max = 255, message = "Title cannot exceed 255 characters.")
        String title,

        @Pattern(
                regexp = "^(https?://[\\w.-]+(?:\\.[\\w\\.-]+)+[/\\w\\.\\-?%&=~#]*)?$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Problem URL must be a valid HTTP or HTTPS address."
        )
        String problemUrl,

        String platform,

        @NotNull(message = "Problem difficulty is required.")
        DSADifficulty difficulty,

        @NotBlank(message = "Primary topic is required.")
        String primaryTopic,

        Set<String> additionalTopics,

        DSAStatus status,

        String personalNotes,

        String solutionApproach,

        String timeComplexity,

        String spaceComplexity,

        Boolean revisionRequired
) {
}

