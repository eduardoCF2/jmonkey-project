package com.TFG1.service;

import com.TFG1.core.cards.Card;
import com.TFG1.core.cards.CardRegistry;
import com.TFG1.core.cards.Suit;
import com.TFG1.model.User;
import com.TFG1.model.UserCard;
import com.TFG1.repository.CardRepository;
import com.TFG1.repository.UserRepository;

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
    }

    // Método que devuelve las cartas que están a la venta hoy
    public CardRegistry getCardRegistry() {
        return cardRegistry;
    }

    public List<Card> getDailyShop() {
        refreshShopIfNeeded();
        return dailyShop;
    }

    private void refreshShopIfNeeded() {
        long currentDay = java.time.LocalDate.now().toEpochDay();
        
        // Usamos el día como semilla para que sea igual para todos durante 24h
        Random seededRandom = new Random(currentDay);
        
        dailyShop.clear();
        for (int i = 0; i < 5; i++) {
            Suit originSuit = rollForSuitRarity(seededRandom);
            List<Card> possibleCards = cardRegistry.getCardsBySuit(originSuit);
            if (!possibleCards.isEmpty()) {
                int randomIndex = seededRandom.nextInt(possibleCards.size());
                dailyShop.add(possibleCards.get(randomIndex));
            }
        }
    }

    public long getSecondsUntilRefresh() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, nextMidnight).getSeconds();
    }

    // La "Ruleta": Un número del 1 al 100 para ver qué rareza (Palo) nos toca
    private Suit rollForSuitRarity(Random r) {
        int roll = r.nextInt(100) + 1; 

        if (roll <= 50) {
            return Suit.BASTOS;
        } 
        else if (roll <= 80) {
            return Suit.COPAS;
        } 
        else if (roll <= 95) {
            return Suit.ESPADAS;
        } 
        else {
            return Suit.OROS;
        }
    }
    
    // Método para comprar una carta
    public boolean buyCard(User user, int cardId, UserRepository userRepo, CardRepository cardRepo) {
        refreshShopIfNeeded(); // Asegurar que trabajamos con la tienda de hoy
        
        // 1. Validar que la carta que pide esté realmente a la venta HOY
        boolean isOnSale = dailyShop.stream().anyMatch(c -> c.id() == cardId);
        if (!isOnSale) {
            System.out.println("La carta " + cardId + " no está a la venta hoy.");
            return false;
        }
        // 2. Definir un precio dinámico
        Card cardToBuy = dailyShop.stream().filter(c -> c.id() == cardId).findFirst().get();
        int price = calculatePrice(cardToBuy);
        
        // 3. Comprobar si ya tiene la carta (si no es consumible)
        if (!cardToBuy.isConsumable()) {
            java.util.List<UserCard> owned = cardRepo.findByUserId(user.getId());
            boolean alreadyOwned = owned.stream().anyMatch(uc -> uc.getCardId() == cardId);
            if (alreadyOwned) {
                System.out.println("Error: El usuario ya posee esta carta no consumible.");
                return false;
            }
        }

        // 4. Comprobar si tiene pasta suficiente
        if (user.getCoins() >= price) {
            
            // 5. Cobrar la carta
            user.setCoins(user.getCoins() - price);
            userRepo.update(user); 
            // 6. Entregar la carta guardándola en la tabla UserCard
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
        return card.price();
    }
}
