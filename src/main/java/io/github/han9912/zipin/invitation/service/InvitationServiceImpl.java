package io.github.han9912.zipin.invitation.service;

import io.github.han9912.zipin.invitation.dto.InvitationRequest;
import io.github.han9912.zipin.invitation.entity.Invitation;
import io.github.han9912.zipin.invitation.mapper.InvitationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationServiceImpl implements InvitationService {
    @Autowired
    InvitationMapper mapper;

    @Transactional
    public Invitation invite(InvitationRequest request, Long recruiterId) {
        int invitationCount = mapper.getInvitationCount(recruiterId);
        if (invitationCount <= 0) throw new IllegalStateException("邀请次数不足");

        int updated = mapper.decreaseInvitationCount(recruiterId);
        if (updated == 0) throw new IllegalStateException("扣减失败，可能引发并行冲突");

        Invitation invitation = new Invitation();
        invitation.setSeekerId(request.seekerId);
        invitation.setJobId(request.jobId);
        mapper.insert(invitation);
        return invitation;
    }
}
