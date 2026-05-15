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

    private List<Card> dailyShop;

    public ShopService(CardRegistry cardRegistry) {
        this.cardRegistry = cardRegistry;
        this.random = new Random();
        this.dailyShop = new ArrayList<>();

        refreshShop();
    }

    public List<Card> getDailyShop() {
        return dailyShop;
    }

    public void refreshShop() {
        dailyShop.clear();

        for (int i = 0; i < 5; i++) {
            Suit originSuit = rollForSuitRarity();

            List<Card> possibleCards = cardRegistry.getCardsBySuit(originSuit);

            if (!possibleCards.isEmpty()) {
                int randomIndex = random.nextInt(possibleCards.size());
                dailyShop.add(possibleCards.get(randomIndex));
            }
        }
        System.out.println("La tienda se ha refrescado con " + dailyShop.size() + " cartas.");
    }

    private Suit rollForSuitRarity() {
        int roll = random.nextInt(100) + 1;

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

    public boolean buyCard(User user, int cardId, UserRepository userRepo, CardRepository cardRepo) {

        boolean isOnSale = dailyShop.stream().anyMatch(c -> c.id() == cardId);
        if (!isOnSale) {
            System.out.println("La carta " + cardId + " no está a la venta hoy.");
            return false;
        }

        Card cardToBuy = dailyShop.stream().filter(c -> c.id() == cardId).findFirst().get();
        int price = calculatePrice(cardToBuy);

        if (user.getCoins() >= price) {

            user.setCoins(user.getCoins() - price);
            userRepo.update(user);

            UserCard newInventoryCard = new UserCard(user, cardId);
            cardRepo.save(newInventoryCard);

            System.out.println("¡Compra exitosa! " + user.getUsername() + " ha comprado la carta " + cardId);
            return true;
        } else {
            System.out.println("Error: " + user.getUsername() + " no tiene suficientes monedas.");
            return false;
        }
    }

    private int calculatePrice(Card card) {
        int basePrice = 0;

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

        double multiplier = 1.0 + (card.value() * 0.05);

        int finalPrice = (int) (basePrice * multiplier);

        return finalPrice;
    }
}
