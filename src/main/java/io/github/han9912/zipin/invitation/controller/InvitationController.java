package io.github.han9912.zipin.invitation.controller;

import io.github.han9912.zipin.common.aspect.annotation.CheckRole;
import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.common.util.AuthUtil;
import io.github.han9912.zipin.invitation.dto.InvitationRequest;
import io.github.han9912.zipin.invitation.entity.Invitation;
import io.github.han9912.zipin.invitation.service.InvitationService;
import io.github.han9912.zipin.user.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {
    @Autowired
    AuthUtil authUtil;
    @Autowired
    InvitationService service;

    @CheckRole(Role.RECRUITER)
    @PostMapping
    public Result<Invitation> invite(@RequestBody InvitationRequest req,
                                     @RequestHeader("Authorization") String auth) {
        return Result.ok(service.invite(req, authUtil.resolveUid(auth)));
    }

}
