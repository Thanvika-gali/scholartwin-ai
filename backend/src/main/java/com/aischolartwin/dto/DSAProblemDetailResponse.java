package com.aischolartwin.dto;

import java.util.List;

public record DSAProblemDetailResponse(
        DSAProblemResponse problem,
        List<DSAAttemptResponse> attempts,
        List<DSARevisionResponse> revisions,
        DSAAttemptStatsResponse stats
) {
}

