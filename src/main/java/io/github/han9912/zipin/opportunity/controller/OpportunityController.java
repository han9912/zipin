package io.github.han9912.zipin.opportunity.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.opportunity.dto.CreateOpportunityRequest;
import io.github.han9912.zipin.opportunity.dto.OpportunityResponse;
import io.github.han9912.zipin.opportunity.service.OpportunityService;
import io.github.han9912.zipin.opportunity.security.TelegramUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {
    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public Result<List<OpportunityResponse>> findLatest(
            @RequestParam(required = false) String keyword
    ) {
        return Result.ok(opportunityService.findLatest(keyword));
    }

    @PostMapping
    public ResponseEntity<Result<OpportunityResponse>> create(
            @Valid @RequestBody CreateOpportunityRequest request,
            @AuthenticationPrincipal TelegramUserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.ok(opportunityService.create(request, principal.getId())));
    }
}
