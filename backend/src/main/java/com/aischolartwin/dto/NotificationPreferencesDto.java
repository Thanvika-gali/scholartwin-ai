package com.aischolartwin.dto;

public record NotificationPreferencesDto(
        Boolean revisionReminders,
        Boolean dailyStudyReminders,
        Boolean careerActionAlerts,
        Boolean processingFailureAlerts
) {}

