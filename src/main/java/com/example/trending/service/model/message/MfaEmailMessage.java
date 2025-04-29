package com.example.trending.service.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MfaEmailMessage {
    private Long userId;
    private String email;
    private String mfaCode;
}
