package io.github.han9912.zipin.opportunity.service;

import io.github.han9912.zipin.opportunity.dto.CreateOpportunityRequest;
import io.github.han9912.zipin.opportunity.dto.OpportunityResponse;
import io.github.han9912.zipin.opportunity.entity.Opportunity;
import io.github.han9912.zipin.opportunity.mapper.OpportunityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpportunityService {
    private static final int MAX_LATEST_OPPORTUNITIES = 100;

    private final OpportunityMapper opportunityMapper;

    public OpportunityService(OpportunityMapper opportunityMapper) {
        this.opportunityMapper = opportunityMapper;
    }

    @Transactional
    public OpportunityResponse create(CreateOpportunityRequest request, Long publisherId) {
        Opportunity opportunity = new Opportunity();
        opportunity.setCategory(request.category.trim());
        opportunity.setTitle(request.title.trim());
        opportunity.setContent(request.content.trim());
        opportunity.setContact(request.contact.trim());
        opportunity.setPublisherId(publisherId);

        if (opportunityMapper.insert(opportunity) != 1) {
            throw new IllegalStateException("招聘信息未能保存");
        }

        Opportunity savedOpportunity = opportunityMapper.findById(opportunity.getId());
        if (savedOpportunity == null) {
            throw new IllegalStateException("招聘信息已写入但读取失败");
        }
        return toResponse(savedOpportunity);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> findLatest() {
        return findLatest(null);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> findLatest(String keyword) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        return opportunityMapper.findLatest(normalizedKeyword, MAX_LATEST_OPPORTUNITIES)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OpportunityResponse toResponse(Opportunity opportunity) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getCategory(),
                opportunity.getTitle(),
                opportunity.getContent(),
                opportunity.getContact(),
                opportunity.getCreatedAt()
        );
    }
}
