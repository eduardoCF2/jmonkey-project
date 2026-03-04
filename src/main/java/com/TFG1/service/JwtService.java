package com.TFG1.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.Date;

public class JwtService {

    // Fabricacion de tokens. Ponemos la contraseña maestra para poder fabricar esos
    // tokens, acceder...

    private static final String SECRET_KEY = "TFG_@EDUARDO_@DAVIDSL_@DAVIDSZ";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    // Método para fabricar el token cuando el login es correcto
    public static String generateToken(String username) {

        try {
            // Token valido por dos horas
            long expirationTime = System.currentTimeMillis() + (1000 * 60 * 60 * 2);

            return JWT.create()
                    .withIssuer("HAY QUE RELLENARLO CUIDAO") // Quién emite el token
                    .withClaim("username", username) // Guardamos el nombre del jugador dentro
                    .withExpiresAt(new Date(expirationTime)) // Fecha de caducidad
                    .sign(ALGORITHM); // Firmado matemáticamente

        } catch (JWTCreationException exception) {
            System.err.println("Error catastrófico al crear el TOKEN");
            throw new RuntimeException("Error interno de seguridad", exception);
        }
    }
}