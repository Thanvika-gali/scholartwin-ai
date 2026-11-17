package com.aischolartwin.dto;

import com.aischolartwin.entity.RuleField;
import com.aischolartwin.entity.RuleOperator;

import java.time.Instant;

public record OrganizationRuleResponse(
        Long id,
        String ruleName,
        RuleField fieldName,
        RuleOperator operator,
        String conditionValue,
        Long targetFolderId,
        String targetFolderName,
        String suggestedTags,
        boolean autoMove,
        int priority,
        boolean isEnabled,
        Instant createdAt,
        Instant updatedAt
) {}

