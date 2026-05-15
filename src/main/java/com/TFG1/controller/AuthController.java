package com.TFG1.controller;

import com.TFG1.exception.GameException;
import com.TFG1.service.AuthService;
import com.TFG1.service.I18nService;
import com.TFG1.service.JwtService;
import com.TFG1.model.User;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class AuthController {

    public record LoginRequest(String username, String password) {
    }

    public record RegisterRequest(String username, String password) {
    }

    public static void registerRoutes(Javalin api) {

        api.post("/api/login", ctx -> {
            try {

                LoginRequest req = ctx.bodyAsClass(LoginRequest.class);

                if (req.username() == null || req.password() == null) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

                AuthService authService = new AuthService();
                User loggedInUser = authService.authenticate(req.username(), req.password());

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

        api.post("/api/register", ctx -> {
            try {

                RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);

                if (req.username() == null || req.password() == null || req.username().isBlank()
                        || req.password().trim().isEmpty()) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

                AuthService authService = new AuthService();
                User newUser = authService.register(req.username(), req.password());

                String lang = getLanguage(ctx);
                String successMsg = I18nService.get(lang, "USER_CREATED_SUCCESS");

                ctx.status(201).json("{ \"message\": \"" + successMsg + "\", \"userId\": " + newUser.getId() + " }");

            } catch (GameException e) {

                handleGameException(e, ctx);

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).json("{ \"error\": \"INTERNAL_SERVER_ERROR\" }");
            }
        });

        api.get("/api/profile", ctx -> {
            try {
                String authHeader = ctx.header("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    ctx.status(401).json("{ \"error\": \"No autorizado\" }");
                    return;
                }
                String token = authHeader.substring(7);
                String username = com.TFG1.service.JwtService.validateToken(token);
                if (username == null) {
                    ctx.status(401).json("{ \"error\": \"Token inválido\" }");
                    return;
                }
                com.TFG1.repository.UserRepository userRepo = new com.TFG1.repository.UserRepository();
                User user = userRepo.findByUsername(username);
                if (user == null) {
                    ctx.status(404).json("{ \"error\": \"Usuario no encontrado\" }");
                    return;
                }
                ctx.status(200).json("{ \"username\": \"" + user.getUsername() + "\", \"coins\": " + user.getCoins() + " }");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).json("{ \"error\": \"INTERNAL_SERVER_ERROR\" }");
            }
        });
    }

    private static String getLanguage(Context ctx) {
        String lang = ctx.header("Accept-Language");
        return (lang != null && lang.length() >= 2) ? lang.substring(0, 2).toUpperCase() : "ES";
    }

    private static void handleGameException(GameException e, Context ctx) {
        String lang = getLanguage(ctx);
        String translatedError = I18nService.get(lang, e.getErrorKey());

        if (ctx.statusCode() == 0 || ctx.statusCode() == 200) {
            ctx.status(400);
        }

        ctx.json("{ \"error\": \"" + translatedError + "\" }");
    }
}

