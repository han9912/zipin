package io.github.han9912.zipin.opportunity.mapper;

import io.github.han9912.zipin.opportunity.entity.Opportunity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OpportunityMapper {
    int insert(Opportunity opportunity);

    Opportunity findById(@Param("id") Long id);

    List<Opportunity> findLatest(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}
