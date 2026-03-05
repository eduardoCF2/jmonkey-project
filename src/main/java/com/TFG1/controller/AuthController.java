package com.TFG1.controller;

import com.TFG1.exception.GameException;
import com.TFG1.service.AuthService;
import com.TFG1.service.I18nService;
import com.TFG1.service.JwtService;
import com.TFG1.model.User;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class AuthController {

    // Uso de DTO's para leer los JSON
    public record LoginRequest(String username, String password) {
    }

    public record RegisterRequest(String username, String password) {
    }

    public static void registerRoutes(Javalin api) {

        // ENDPOINT PARA INICIAR SESION

        api.post("/api/login", ctx -> {
            try {
                // Lee el JSON del cliente
                LoginRequest req = ctx.bodyAsClass(LoginRequest.class);

                if (req.username() == null || req.password() == null) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

                // LLamada a la base de datos
                AuthService authService = new AuthService();
                User loggedInUser = authService.authenticate(req.username(), req.password());

                // CODIGO 200 EXITO
                String lang = getLanguage(ctx);
                String successMsg = I18nService.get(lang, "CORRECT_LOGIN");

                String tokenReal = JwtService.generateToken(loggedInUser.getUsername());

                ctx.status(200).json("{ \"message\": \"" + successMsg + "\", \"token\": \"" + tokenReal + "\" }");

            } catch (GameException e) {
                handleGameException(e, ctx);
            } catch (Exception e) {
                ctx.status(500).json("{ \"error\": \"Fallo interno del servidor\" }");
                e.printStackTrace();
            }
        });

        // Endpoint para registrarse

        api.post("/api/register", ctx -> {
            try {
                // Convertir el JSON de la peticion a nuestro objeto Java
                RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);

                // Validacion básica de nulos
                if (req.username() == null || req.password() == null || req.username().isBlank()
                        || req.password().trim().isEmpty()) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

                // LLamada al servicio (comprobacion real que hereda de service...)
                AuthService authService = new AuthService();
                User newUser = authService.register(req.username(), req.password());

                String lang = getLanguage(ctx);
                String successMsg = I18nService.get(lang, "USER_CREATED_SUCCESS");

                ctx.status(201).json("{ \"message\": \"" + successMsg + "\", \"userId\": " + newUser.getId() + " }");

            } catch (GameException e) {

                // Lanza excepcion, ya sea un 400 0 401 asi q

                handleGameException(e, ctx);

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).json("{ \"error\": \"INTERNAL_SERVER_ERROR\" }");
            }
        });
    }

    // METODOS AUXILIARES OJO

    // Lee la cabecera HTTP para saber el idioma del jugador
    private static String getLanguage(Context ctx) {
        String lang = ctx.header("Accept-Language");
        return (lang != null && lang.length() >= 2) ? lang.substring(0, 2).toUpperCase() : "ES";
    }

    // Se encarga de traducir la GameException y enviarla
    private static void handleGameException(GameException e, Context ctx) {
        String lang = getLanguage(ctx);
        String translatedError = I18nService.get(lang, e.getErrorKey());

        // Si no le ponemos un codigo de estado especifico arriba, le ponemos 400 por
        // defecto
        if (ctx.statusCode() == 0 || ctx.statusCode() == 200) {
            ctx.status(400);
        }

        ctx.json("{ \"error\": \"" + translatedError + "\" }");
    }
}

// REVISAR POR QUE SALE DE GEMINI PERO ES UNA FUMADA