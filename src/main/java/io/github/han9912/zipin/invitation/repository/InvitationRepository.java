package io.github.han9912.zipin.invitation.repository;

import io.github.han9912.zipin.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
}
