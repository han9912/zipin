package io.github.han9912.zipin.user.dto;

import io.github.han9912.zipin.user.entity.Role;

public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public Role role;
}
