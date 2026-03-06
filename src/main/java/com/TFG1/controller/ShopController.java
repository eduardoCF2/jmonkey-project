package com.TFG1.controller;

import com.TFG1.model.User;
import com.TFG1.repository.CardRepository;
import com.TFG1.repository.UserRepository;
import com.TFG1.service.ShopService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ShopController {

    private final ShopService shopService;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public ShopController(ShopService shopService, UserRepository userRepository, CardRepository cardRepository) {
        this.shopService = shopService;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    // Método para registrar las rutas en tu aplicación Javalin
    public void registerRoutes(Javalin app) {
        
        // 1. GET /shop/daily -> Devuelve el JSON con las 5 cartas de hoy
        app.get("/shop/daily", this::getDailyShop);
        
        // 2. POST /shop/buy -> El usuario intenta comprar una carta
        app.post("/shop/buy", this::buyCardEndpoint);
    }

    private void getDailyShop(Context ctx) {
        // Simplemente respondemos devolviendo la lista convertida a JSON
        // Para que el cliente visualice la tienda
        ctx.json(shopService.getDailyShop());
    }

    private void buyCardEndpoint(Context ctx) {
        // En una app real, sacaríamos quién es el User desde el JWT (Auth)
        // Por ahora, para probar, imaginemos que nos pasan su nombre y la carta por JSON o QueryParam
        
        String username = ctx.queryParam("username"); 
        String cardIdStr = ctx.queryParam("cardId");

        if (username == null || cardIdStr == null) {
            ctx.status(400).result("Faltan parámetros (username o cardId)");
            return;
        }

        int cardId = Integer.parseInt(cardIdStr);
        User user = userRepository.findByUsername(username);

        if (user == null) {
            ctx.status(404).result("Usuario no encontrado");
            return;
        }

        // Llamamos al cerebro del Servicio que hemos creado hoy
        boolean success = shopService.buyCard(user, cardId, userRepository, cardRepository);

        if (success) {
            ctx.status(200).result("Compra completada. Monedas restantes: " + user.getCoins());
        } else {
            ctx.status(400).result("La compra falló (o no está en la tienda, o faltan monedas)");
        }
    }
}
