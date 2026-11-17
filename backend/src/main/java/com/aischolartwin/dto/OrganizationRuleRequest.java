package com.aischolartwin.dto;

import com.aischolartwin.entity.RuleField;
import com.aischolartwin.entity.RuleOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationRuleRequest(
        @NotBlank(message = "Rule name is required")
        @Size(max = 150, message = "Rule name must not exceed 150 characters")
        String ruleName,

        @NotNull(message = "Target field is required")
        RuleField fieldName,

        @NotNull(message = "Operator is required")
        RuleOperator operator,

        @NotBlank(message = "Condition value is required")
        @Size(max = 255)
        String conditionValue,

        @NotNull(message = "Target folder is required")
        Long targetFolderId,

        String suggestedTags,
        Boolean autoMove,
        Integer priority,
        Boolean isEnabled
) {}

