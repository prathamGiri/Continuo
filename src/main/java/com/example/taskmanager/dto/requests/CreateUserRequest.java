package com.example.taskmanager.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Must Provide a Name")
    private String userName;
    @NotBlank(message = "Must Provide an Email")
    @Email(message = "Invalid Email Format")
    private String emailId;
    @NotBlank(message = "Must Provide an Password")
    private String password;
}
