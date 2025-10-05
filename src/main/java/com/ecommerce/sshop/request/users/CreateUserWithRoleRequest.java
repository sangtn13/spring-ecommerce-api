package com.ecommerce.sshop.request.users;

import lombok.Data;

@Data
public class CreateUserWithRoleRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role; // "User" or "Admin"
}