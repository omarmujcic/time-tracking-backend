package com.omarmujcic.timetracking.config.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createToken(String username) {
        try {
            String header = encode(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encode(Map.of(
                "sub", username,
                "iat", Instant.now().getEpochSecond(),
                "exp", Instant.now().plusSeconds(properties.getExpirationMinutes() * 60).getEpochSecond()
            ));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create JWT", exception);
        }
    }

    public String getValidSubject(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!sign(unsignedToken).equals(parts[2])) {
                return null;
            }

            Map<String, Object> claims = objectMapper.readValue(
                BASE64_URL_DECODER.decode(parts[1]),
                new TypeReference<>() {
                }
            );
            Number expiration = (Number) claims.get("exp");
            if (expiration == null || expiration.longValue() < Instant.now().getEpochSecond()) {
                return null;
            }

            Object subject = claims.get("sub");
            return subject instanceof String username ? username : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private String encode(Map<String, ?> value) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }
}
