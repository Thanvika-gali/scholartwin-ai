package com.aischolartwin.dto;

import java.time.LocalDate;

public record DSAActivityTimelineItem(
        LocalDate date,
        long attemptsCount,
        long successfulAttemptsCount,
        long failedAttemptsCount
) {
}

