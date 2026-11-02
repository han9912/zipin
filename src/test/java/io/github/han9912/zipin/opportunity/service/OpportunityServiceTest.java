package io.github.han9912.zipin.opportunity.service;

import io.github.han9912.zipin.opportunity.dto.CreateOpportunityRequest;
import io.github.han9912.zipin.opportunity.dto.OpportunityResponse;
import io.github.han9912.zipin.opportunity.entity.Opportunity;
import io.github.han9912.zipin.opportunity.mapper.OpportunityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {
    @Mock
    private OpportunityMapper opportunityMapper;

    @InjectMocks
    private OpportunityService opportunityService;

    @Test
    void createWritesThenReadsSavedOpportunity() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 2, 14, 0);
        Opportunity stored = opportunity(42L, "兼职", "周末活动协助", "协助现场活动", "@contact", createdAt);
        when(opportunityMapper.insert(any())).thenAnswer(invocation -> {
            Opportunity inserted = invocation.getArgument(0);
            inserted.setId(42L);
            return 1;
        });
        when(opportunityMapper.findById(42L)).thenReturn(stored);

        CreateOpportunityRequest request = new CreateOpportunityRequest();
        request.category = "  兼职  ";
        request.title = "  周末活动协助  ";
        request.content = "  协助现场活动  ";
        request.contact = "  @contact  ";

        OpportunityResponse response = opportunityService.create(request, 9L);

        ArgumentCaptor<Opportunity> captor = ArgumentCaptor.forClass(Opportunity.class);
        verify(opportunityMapper).insert(captor.capture());
        verify(opportunityMapper).findById(42L);
        assertEquals("兼职", captor.getValue().getCategory());
        assertEquals("周末活动协助", captor.getValue().getTitle());
        assertEquals(9L, captor.getValue().getPublisherId());
        assertEquals(42L, response.id());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void findLatestMapsDatabaseRows() {
        Opportunity row = opportunity(
                7L,
                "远程",
                "文案协作",
                "协助整理活动文案",
                "hello@example.test",
                LocalDateTime.of(2026, 11, 1, 10, 0)
        );
        when(opportunityMapper.findLatest(null, 100)).thenReturn(List.of(row));

        List<OpportunityResponse> responses = opportunityService.findLatest();

        assertEquals(1, responses.size());
        assertEquals(row.getId(), responses.get(0).id());
        assertEquals(row.getContent(), responses.get(0).content());
        verify(opportunityMapper).findLatest(eq(null), eq(100));
    }

    private Opportunity opportunity(
            Long id,
            String category,
            String title,
            String content,
            String contact,
            LocalDateTime createdAt
    ) {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(id);
        opportunity.setCategory(category);
        opportunity.setTitle(title);
        opportunity.setContent(content);
        opportunity.setContact(contact);
        opportunity.setCreatedAt(createdAt);
        return opportunity;
    }
}
