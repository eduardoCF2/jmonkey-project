package com.TFG1.core.cards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CardRegistry {

    private final Map<Integer, Card> cards = new HashMap<>();

    public CardRegistry() {

    }

    private void initCards() {
        loadCardsFromJson();
    }

    private void loadCardsFromJson() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cards.json")) {
            if (is == null) {
                throw new RuntimeException("Error, no se encontró el archivo json");
            }

            List<Card> cardList = mapper.readValue(is, new TypeReference<List<Card>>() {
            });

            for (Card card : cardList) {
                cards.put(card.id(), card);
            }

            System.out.println("Se ha cargado " + cards.size() + " cartas del json");
        } catch (Exception e) {
            System.out.println("Error al cargar las cartas; " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Card getCardById(int id) {
        return cards.get(id);
    }
    // Método para obtener todas las cartas de un palo en concreto (para la tienda)
    public List<Card> getCardsBySuit(Suit suit) {
        return cards.values().stream()
                .filter(card -> card.suit() == suit)
                .toList(); // Devuelve una lista con todas las cartas de Bastos, o Copas, etc.
    }

}
