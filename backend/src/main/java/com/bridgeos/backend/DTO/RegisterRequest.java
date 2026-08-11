package com.bridgeos.backend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    private  String name;
    private String email;
    private  String role;
    private String password;
}
