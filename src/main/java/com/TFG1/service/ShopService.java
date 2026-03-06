package com.TFG1.service;

import com.TFG1.core.cards.Card;
import com.TFG1.core.cards.CardRegistry;
import com.TFG1.core.cards.Suit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.TFG1.model.User;
import com.TFG1.model.UserCard;
import com.TFG1.repository.CardRepository;
import com.TFG1.repository.UserRepository;

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
        // 2. Definir un precio dinámico
        Card cardToBuy = dailyShop.stream().filter(c -> c.id() == cardId).findFirst().get();
        int price = calculatePrice(cardToBuy);
        
        // 3. Comprobar si tiene pasta suficiente
        if (user.getCoins() >= price) {
            
            // 4. Cobrar la carta
            user.setCoins(user.getCoins() - price);
            userRepo.update(user); 
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

    // Método que calcula el precio de una carta en base a su rareza y su número
    private int calculatePrice(Card card) {
        int basePrice = 0;
        
        // 1. Primero el precio base por RAREZA (Palo)
        switch (card.suit()) {
            case BASTOS:
                basePrice = 10;
                break;
            case COPAS:
                basePrice = 25;
                break;
            case ESPADAS:
                basePrice = 50;
                break;
            case OROS:
                basePrice = 100;
                break;
        }

        // 2. Sumamos un plus por el VALOR / POTENCIA de la carta (número del A al Joker)
        // Imagino que value() es por ejemplo 1 (As), 12 (Rey)... o más para el comodín.
        // Por cada punto de valor, la carta es un 5% más cara.
        // Si tienes Jokers con un valor fijo muy alto (ej. 15), costarán más.
        
        double multiplier = 1.0 + (card.value() * 0.05); // 1 = 1.05x, 10 = 1.5x, 15 = 1.75x
        
        int finalPrice = (int) (basePrice * multiplier);
        
        return finalPrice;
    }

}
