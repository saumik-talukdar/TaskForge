package com.saumik.TaskForge.domain.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public record RegistrationRequest (
    String fullName,
    String email,
    String password
){}
