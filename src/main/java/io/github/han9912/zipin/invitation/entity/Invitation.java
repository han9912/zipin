package io.github.han9912.zipin.invitation.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static io.github.han9912.zipin.invitation.entity.InvitationStatus.PENDING;

@Setter
@Getter
public class Invitation {
    private Long id;
    private Long seekerId;
    private Long jobId;
    private Long recruiterId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private InvitationStatus status = PENDING;
}
