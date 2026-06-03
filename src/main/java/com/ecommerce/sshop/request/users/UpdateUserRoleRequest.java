package com.ecommerce.sshop.request.users;

import java.util.Set;

import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    private Set<String> roles;
}
