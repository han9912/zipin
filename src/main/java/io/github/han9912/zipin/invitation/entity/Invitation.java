package io.github.han9912.zipin.invitation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static io.github.han9912.zipin.invitation.entity.InvitationStatus.PENDING;

@Entity
@Table(name = "invitations")
@Setter
@Getter
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long seekerId;
    private Long jobId;
    private Long recruiterId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private InvitationStatus status = PENDING;
}
