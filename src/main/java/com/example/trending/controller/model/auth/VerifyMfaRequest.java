package com.example.trending.controller.model.auth;

import lombok.Data;

@Data
public class VerifyMfaRequest {
    private String email;
    private String mfaCode;
}
