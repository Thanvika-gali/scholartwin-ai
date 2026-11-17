package com.aischolartwin.dto;

import java.util.List;

public record AcademicPreferencesDto(
        Integer targetSemester,
        Integer targetGraduationYear,
        String primaryCareerTarget,
        List<String> secondaryRoles,
        List<String> technicalInterests,
        Integer weeklyDsaTarget,
        Integer dailyReadingMinutes,
        String preferredStudyTime
) {}

