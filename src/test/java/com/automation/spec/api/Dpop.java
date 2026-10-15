package com.automation.spec.api;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.UUID;

/**
 * DPoP (RFC 9449) proof builder. The HOVR IAM service binds the refresh token to the key that signed the sign-in
 * call, and RefreshAccessToken is rejected ("dpop proof required" / "token not bound to this key") without a proof
 * from that same key. One instance == one key == one rider session.
 */
public final class Dpop {

    private final KeyPair keys;
    private final String jwkJson;

    public Dpop() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(new ECGenParameterSpec("secp256r1"));
            keys = gen.generateKeyPair();
            ECPublicKey pub = (ECPublicKey) keys.getPublic();
            jwkJson = "{\"kty\":\"EC\",\"crv\":\"P-256\",\"x\":\"" + b64(fixed32(pub.getW().getAffineX()))
                    + "\",\"y\":\"" + b64(fixed32(pub.getW().getAffineY())) + "\"}";
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create DPoP key", e);
        }
    }

    private static byte[] fixed32(BigInteger n) {
        byte[] raw = n.toByteArray();
        byte[] out = new byte[32];
        int copy = Math.min(raw.length, 32);
        System.arraycopy(raw, raw.length - copy, out, 32 - copy, copy);
        return out;
    }

    private static String b64(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** A proof for one POST to `htu`; `accessToken` (optional) adds the access-token hash claim. */
    public String proof(String htu, String accessToken) {
        try {
            String header = "{\"typ\":\"dpop+jwt\",\"alg\":\"ES256\",\"jwk\":" + jwkJson + "}";
            StringBuilder payload = new StringBuilder("{\"htm\":\"POST\",\"htu\":\"").append(htu)
                    .append("\",\"iat\":").append(System.currentTimeMillis() / 1000)
                    .append(",\"jti\":\"").append(UUID.randomUUID()).append("\"");
            if (accessToken != null) {
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(accessToken.getBytes(StandardCharsets.US_ASCII));
                payload.append(",\"ath\":\"").append(b64(hash)).append("\"");
            }
            payload.append("}");
            String signingInput = b64(header.getBytes(StandardCharsets.UTF_8)) + "."
                    + b64(payload.toString().getBytes(StandardCharsets.UTF_8));
            Signature sig = Signature.getInstance("SHA256withECDSAinP1363Format");
            sig.initSign(keys.getPrivate());
            sig.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            return signingInput + "." + b64(sig.sign());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot build DPoP proof", e);
        }
    }
}
