package io.github.han9912.zipin.invitation.service;

import io.github.han9912.zipin.invitation.dto.InvitationRequest;
import io.github.han9912.zipin.invitation.entity.Invitation;
import org.springframework.stereotype.Service;

@Service
public interface InvitationService {
    public Invitation invite(InvitationRequest request, Long recruiterId);
}
