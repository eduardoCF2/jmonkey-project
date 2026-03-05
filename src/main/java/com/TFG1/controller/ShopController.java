package com.TFG1.controller;

import com.TFG1.exception.GameException;
import com.TFG1.model.User;
import com.TFG1.repository.UserRepository;
import com.TFG1.service.I18nService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class ShopController {

    // Mock Database for shop items
    public record ShopItem(String id, String nameKey, int price) {
    }

    private static final List<ShopItem> CATALOGUE = new ArrayList<>();

    static {
        CATALOGUE.add(new ShopItem("gold_dice", "ITEM_GOLD_DICE", 50));
        CATALOGUE.add(new ShopItem("red_card", "ITEM_RED_CARD", 30));
    }

    public record BuyRequest(int userId, String itemId) {
    }

    public static void registerRoutes(Javalin api) {

        // Endpoint para ver los artículos
        api.get("/api/shop/items", ctx -> {
            ctx.status(200).json(CATALOGUE);
        });

        // Endpoint para comprar un artículo
        api.post("/api/shop/buy", ctx -> {
            try {
                BuyRequest req = ctx.bodyAsClass(BuyRequest.class);

                if (req.itemId() == null || req.userId() <= 0) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

                // 1. Buscar al item
                ShopItem itemToBuy = CATALOGUE.stream()
                        .filter(item -> item.id().equals(req.itemId()))
                        .findFirst()
                        .orElse(null);

                if (itemToBuy == null) {
                    ctx.status(404);
                    throw new GameException("ERROR_ITEM_NOT_FOUND");
                }

                UserRepository userRepo = new UserRepository();
                // OJO: Por ahora no tenemos `findById` en UserRepository,
                // idealmente deberiamos añadirlo para buscar por ID en vez de nombre.
                // Lo simulamos temporalmente con una nueva excepcion si el repositorio no
                // encuentra al usuario.
                User buyer = userRepo.findById(req.userId());

                if (buyer == null) {
                    ctx.status(404);
                    throw new GameException("USER_NOT_FOUND");
                }

                // 2. Comprobar monedas
                if (buyer.getCoins() < itemToBuy.price()) {
                    ctx.status(400);
                    throw new GameException("ERROR_INSUFFICIENT_FUNDS");
                }

                // 3. Efectuar compra
                buyer.setCoins(buyer.getCoins() - itemToBuy.price());
                userRepo.update(buyer);

                String lang = getLanguage(ctx);
                String successMsg = I18nService.get(lang, "PURCHASE_SUCCESS_MSG");

                ctx.status(200)
                        .json("{ \"message\": \"" + successMsg + "\", \"newBalance\": " + buyer.getCoins() + " }");

            } catch (GameException e) {
                handleGameException(e, ctx);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).json("{ \"error\": \"INTERNAL_SERVER_ERROR\" }");
            }
        });
    }

    // METODOS AUXILIARES: se reutilizan del AuthController, en un código "limpio"
    // irían a un base class o Helper.

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
