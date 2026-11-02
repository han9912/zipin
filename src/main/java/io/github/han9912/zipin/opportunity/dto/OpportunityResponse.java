package io.github.han9912.zipin.opportunity.dto;

import java.time.LocalDateTime;

public record OpportunityResponse(
        Long id,
        String category,
        String title,
        String content,
        String contact,
        LocalDateTime createdAt
) {
}
