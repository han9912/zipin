package io.github.han9912.zipin.invitation.service;

import io.github.han9912.zipin.invitation.dto.InvitationRequest;
import io.github.han9912.zipin.invitation.entity.Invitation;
import io.github.han9912.zipin.invitation.repository.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvitationServiceImpl implements InvitationService {
    @Autowired
    InvitationRepository repo;

    public Invitation invite(InvitationRequest request, Long recruiterId) {
        Invitation invitation = new Invitation();
        invitation.setSeekerId(request.seekerId);
        invitation.setJobId(request.jobId);
        return repo.save(invitation);
    }
}
