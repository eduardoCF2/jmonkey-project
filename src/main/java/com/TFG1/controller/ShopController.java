package com.TFG1.controller;

import com.TFG1.exception.GameException;
import com.TFG1.model.User;
import com.TFG1.repository.CardRepository;
import com.TFG1.repository.UserRepository;
import com.TFG1.service.I18nService;
import com.TFG1.service.ShopService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class ShopController {

    public record ShopItem(String id, String nameKey, int price) {
    }

    private static final List<ShopItem> CATALOGUE = new ArrayList<>();

    static {
        CATALOGUE.add(new ShopItem("gold_dice", "ITEM_GOLD_DICE", 50));
        CATALOGUE.add(new ShopItem("red_card", "ITEM_RED_CARD", 30));
    }

    public record BuyRequest(int userId, String itemId) {
    }

    public record BuyCardRequest(int cardId) {
    }

    private final ShopService shopService;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public ShopController(ShopService shopService, UserRepository userRepository, CardRepository cardRepository) {
        this.shopService = shopService;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    public void registerRoutes(Javalin api) {

        api.get("/api/shop/items", ctx -> {
            ctx.status(200).json(CATALOGUE);
        });

        api.post("/api/shop/buy", ctx -> {
            try {
                BuyRequest req = ctx.bodyAsClass(BuyRequest.class);

                if (req.itemId() == null || req.userId() <= 0) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

                ShopItem itemToBuy = CATALOGUE.stream()
                        .filter(item -> item.id().equals(req.itemId()))
                        .findFirst()
                        .orElse(null);

                if (itemToBuy == null) {
                    ctx.status(404);
                    throw new GameException("ERROR_ITEM_NOT_FOUND");
                }

                User buyer = userRepository.findById(req.userId());

                if (buyer == null) {
                    ctx.status(404);
                    throw new GameException("USER_NOT_FOUND");
                }

                if (buyer.getCoins() < itemToBuy.price()) {
                    ctx.status(400);
                    throw new GameException("ERROR_INSUFFICIENT_FUNDS");
                }

                buyer.setCoins(buyer.getCoins() - itemToBuy.price());
                userRepository.update(buyer);

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

        api.get("/api/shop/cards", ctx -> {
            ctx.status(200).json(shopService.getDailyShop());
        });

        api.post("/api/shop/buy-card", ctx -> {
            try {
                BuyCardRequest req = ctx.bodyAsClass(BuyCardRequest.class);

                if (req.cardId() <= 0) {
                    ctx.status(400);
                    throw new GameException("ERROR_INVALID_DATA");
                }

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

                User buyer = userRepository.findByUsername(username);

                if (buyer == null) {
                    ctx.status(404);
                    throw new GameException("USER_NOT_FOUND");
                }

                boolean success = shopService.buyCard(buyer, req.cardId(), userRepository, cardRepository);
                if (success) {
                    String lang = getLanguage(ctx);
                    String successMsg = I18nService.get(lang, "PURCHASE_SUCCESS_MSG");
                    ctx.status(200).json("{ \"message\": \"" + successMsg + "\", \"newBalance\": " + buyer.getCoins() + " }");
                } else {
                    ctx.status(400);
                    throw new GameException("ERROR_INSUFFICIENT_FUNDS");
                }
            } catch (GameException e) {
                handleGameException(e, ctx);
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
