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
                
                // Cheat: Grant all cards and coins to player (DISABLED)
                // grantCheatAllCardsAndCoins(loggedInUser);

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
                
                // Cheat: Grant all cards and coins to player (DISABLED)
                // grantCheatAllCardsAndCoins(newUser);

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

    private static void grantCheatAllCardsAndCoins(User user) {
        try {
            com.TFG1.repository.UserRepository userRepo = new com.TFG1.repository.UserRepository();
            com.TFG1.repository.CardRepository cardRepo = new com.TFG1.repository.CardRepository();
            
            // Set coins
            user.setCoins(9999);
            userRepo.update(user);
            
            // Get existing card IDs owned by user
            java.util.List<com.TFG1.model.UserCard> existing = cardRepo.findByUserId(user.getId());
            java.util.Set<Integer> ownedCardIds = new java.util.HashSet<>();
            for (com.TFG1.model.UserCard uc : existing) {
                ownedCardIds.add(uc.getCardId());
            }
            
            // Add all cards (1 to 40) if they are not already owned
            for (int i = 1; i <= 40; i++) {
                if (!ownedCardIds.contains(i)) {
                    com.TFG1.model.UserCard uc = new com.TFG1.model.UserCard(user, i);
                    cardRepo.save(uc);
                }
            }
            System.out.println("[CHEAT] Otorgadas todas las cartas y monedas al usuario: " + user.getUsername());
        } catch (Exception e) {
            System.err.println("[CHEAT ERROR] No se pudieron otorgar cartas/monedas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

