package com.TFG1.service;

import com.TFG1.core.cards.Card;
import com.TFG1.core.cards.CardRegistry;
import com.TFG1.core.cards.Suit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopService {

    private final CardRegistry cardRegistry;
    private final Random random;

    // Aquí guardaremos las 5 cartas de la tienda de hoy
    private List<Card> dailyShop;

    public ShopService(CardRegistry cardRegistry) {
        this.cardRegistry = cardRegistry;
        this.random = new Random();
        this.dailyShop = new ArrayList<>();
        
        // Al arrancar el servicio por primera vez, generamos la tienda
        refreshShop();
    }

    // Método que devuelve las cartas que están a la venta hoy
    public List<Card> getDailyShop() {
        return dailyShop;
    }

    // Lógica principal: Rellenar la tienda con 5 cartas nuevas basadas en probabilidad
    public void refreshShop() {
        dailyShop.clear(); // Limpiamos la tienda anterior

        // Queremos generar exactamente 5 cartas (variable a posterior pueden ser más)
        for (int i = 0; i < 5; i++) {
            Suit originSuit = rollForSuitRarity();
            
            // Le pedimos al CardRegistry TODAS las cartas de esa rareza
            List<Card> possibleCards = cardRegistry.getCardsBySuit(originSuit);
            
            // Cogemos una al azar de entre ellas (si hay alguna)
            if (!possibleCards.isEmpty()) {
                int randomIndex = random.nextInt(possibleCards.size());
                dailyShop.add(possibleCards.get(randomIndex));
            }
        }
        System.out.println("La tienda se ha refrescado con " + dailyShop.size() + " cartas.");
    }

    // La "Ruleta": Un número del 1 al 100 para ver qué rareza (Palo) nos toca
    private Suit rollForSuitRarity() {
        int roll = random.nextInt(100) + 1; // Número aleatorio de 1 a 100

        // 50% de probabilidad para Bastos (Comunidad inicial, la más cutre)
        if (roll <= 50) {
            return Suit.BASTOS;
        } 
        // 30% de probabilidad para Copas (Poco común)
        else if (roll <= 80) {
            return Suit.COPAS;
        } 
        // 15% de probabilidad para Espadas (Rara)
        else if (roll <= 95) {
            return Suit.ESPADAS;
        } 
        // 5% de probabilidad para Oros (Legendaria, la joya de la corona)
        else {
            return Suit.OROS;
        }
    }
    //Método para comprar una carta
     public boolean buyCard(User user, int cardId, UserRepository userRepo, CardRepository cardRepo) {
        
        // 1. Validar que la carta que pide esté realmente a la venta HOY
        boolean isOnSale = dailyShop.stream().anyMatch(c -> c.id() == cardId);
        if (!isOnSale) {
            System.out.println("La carta " + cardId + " no está a la venta hoy.");
            return false;
        }
        // 2. Definir un precio (para empezar podemos decir que todas cuestan 20 monedas,
        // o podrías hacerlo dinámico según la rareza de la carta)
        int price = 20;
        // 3. Comprobar si tiene pasta suficiente
        if (user.getCoins() >= price) {
            
            // 4. Cobrar la carta
            user.setCoins(user.getCoins() - price);
            userRepo.update(user); // ¡Ojo! Tendrás que crear este método update en UserRepository
            // 5. Entregar la carta guardándola en la tabla UserCard
            UserCard newInventoryCard = new UserCard(user, cardId);
            cardRepo.save(newInventoryCard);
            
            System.out.println("¡Compra exitosa! " + user.getUsername() + " ha comprado la carta " + cardId);
            return true;
        } else {
            System.out.println("Error: " + user.getUsername() + " no tiene suficientes monedas.");
            return false;
        }
}
