package com.example.trending.service;

import com.example.trending.db.model.MFACode;
import com.example.trending.db.model.User;
import com.example.trending.db.repository.MFARepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class MfaService {

    private final MFARepository mfaCodeRepository;

    public MfaService(MFARepository mfaCodeRepository) {
        this.mfaCodeRepository = mfaCodeRepository;
    }

    public MFACode generateMfaCode(User user) {
        // 產生 6 位數字 MFA 驗證碼
        String code = String.format("%06d", new Random().nextInt(999999));

        MFACode mfaCode = new MFACode();
        mfaCode.setUserId(user.getId());
        mfaCode.setCode(code);
        mfaCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        mfaCode.setCreatedAt(LocalDateTime.now());

        return mfaCodeRepository.save(mfaCode);
    }

    public void saveMfaCode(Long userId, String code) {
        // 1. 清除之前舊的 MFA 記錄
        mfaCodeRepository.deleteByUserId(userId);

        // 2. 建立新的 MFA record
        MFACode mfaCode = new MFACode();
        mfaCode.setUserId(userId); // << 注意是 userId，不是 user object
        mfaCode.setCode(code);
        mfaCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        mfaCode.setCreatedAt(LocalDateTime.now());

        // 3. 儲存
        mfaCodeRepository.save(mfaCode);
    }
}
