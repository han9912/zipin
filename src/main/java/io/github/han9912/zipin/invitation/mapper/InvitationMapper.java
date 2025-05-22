package io.github.han9912.zipin.invitation.mapper;

import io.github.han9912.zipin.invitation.entity.Invitation;

public interface InvitationMapper {
    int insert(Invitation invitation);
    int getInvitationCount(Long recruiterId);
    int decreaseInvitationCount(Long recruiterId);
}
