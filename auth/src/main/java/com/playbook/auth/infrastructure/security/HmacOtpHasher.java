package com.playbook.auth.infrastructure.security;

import com.playbook.auth.application.service.OtpHasher;
import com.playbook.auth.config.AuthProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class HmacOtpHasher implements OtpHasher {

    private static final String ALGO = "HmacSHA256";
    private final SecretKeySpec key;

    public HmacOtpHasher(AuthProperties props) {
        this.key = new SecretKeySpec(props.otp().pepper().getBytes(StandardCharsets.UTF_8), ALGO);
    }

    @Override
    public String hash(String phoneNumber, String otp) {
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(key);
            byte[] out = mac.doFinal((phoneNumber + ":" + otp).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean matches(String phoneNumber, String otp, String storedHash) {
        return MessageDigest.isEqual(
                hash(phoneNumber, otp).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}