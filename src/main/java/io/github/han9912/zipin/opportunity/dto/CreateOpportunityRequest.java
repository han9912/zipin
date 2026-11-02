package io.github.han9912.zipin.opportunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateOpportunityRequest {
    @NotBlank
    @Size(max = 80)
    public String category;

    @NotBlank
    @Size(max = 160)
    public String title;

    @NotBlank
    @Size(max = 4000)
    public String content;

    @NotBlank
    @Size(max = 120)
    public String contact;
}
