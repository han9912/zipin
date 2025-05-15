package io.github.han9912.zipin.invitation.service;

import io.github.han9912.zipin.invitation.dto.InvitationRequest;
import io.github.han9912.zipin.invitation.entity.Invitation;

public interface InvitationService {
    Invitation invite(InvitationRequest request, Long recruiterId);
}
