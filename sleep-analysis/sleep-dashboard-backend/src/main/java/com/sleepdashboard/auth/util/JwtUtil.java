package com.sleepdashboard.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET_KEY = "SleepDashboardSecretKeyForJWTAuth2026";
    private static final String CLAIM_KEY_USER_ID = "userId";
    private static final String CLAIM_KEY_USERNAME = "username";
    
    // Token valid for 24 hours
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000L;

    /**
     * Generate Token
     */
    public static String createToken(Long userId, String username) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        return JWT.create()
                .withHeader(header)
                .withClaim(CLAIM_KEY_USER_ID, userId)
                .withClaim(CLAIM_KEY_USERNAME, username)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    /**
     * Verify Token and return Claims
     */
    public static DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (Exception e) {
            return null; // Token verification failed
        }
    }

    /**
     * Get UserId from DecodedJWT
     */
    public static Long getUserId(DecodedJWT decodedJWT) {
        if (decodedJWT == null) return null;
        return decodedJWT.getClaim(CLAIM_KEY_USER_ID).asLong();
    }

    /**
     * Get Username from DecodedJWT
     */
    public static String getUsername(DecodedJWT decodedJWT) {
        if (decodedJWT == null) return null;
        return decodedJWT.getClaim(CLAIM_KEY_USERNAME).asString();
    }
}
