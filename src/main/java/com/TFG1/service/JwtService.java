package com.TFG1.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.Date;

public class JwtService {

    private static final String SECRET_KEY = "TFG_@EDUARDO_@DAVIDSL_@DAVIDSZ";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    public static String generateToken(String username) {

        try {

            long expirationTime = System.currentTimeMillis() + (1000 * 60 * 60 * 2);

            return JWT.create()
                    .withIssuer("HAY QUE RELLENARLO CUIDAO")
                    .withClaim("username", username)
                    .withExpiresAt(new Date(expirationTime))
                    .sign(ALGORITHM);

        } catch (JWTCreationException exception) {
            System.err.println("Error catastrófico al crear el TOKEN");
            throw new RuntimeException("Error interno de seguridad", exception);
        }
    }

    public static String validateToken(String token) {
        try {
            return JWT.require(ALGORITHM)
                    .withIssuer("HAY QUE RELLENARLO CUIDAO")
                    .build()
                    .verify(token)
                    .getClaim("username").asString();
        } catch (Exception exception) {
            throw new RuntimeException("Token inválido o expirado");
        }
    }
}