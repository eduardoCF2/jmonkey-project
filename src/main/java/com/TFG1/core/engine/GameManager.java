package com.TFG1.core.engine;

import com.TFG1.core.dice.Bid;
import com.TFG1.core.dice.DiceLogic;
import com.TFG1.core.dice.Die;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.TFG1.core.cards.Card;
import com.TFG1.core.cards.CardRegistry;
import com.TFG1.core.cards.CardType;

public class GameManager {

    private final List<Player> players;
    private GameState state;
    private int currentPlayerIndex;
    private Bid currentBid;
    private Player lastBidder;
    private final DiceLogic diceLogic;
    private boolean skipNextTurn = false;
    private boolean hasPlayedCardThisTurn = false; // Nueva restricción
    private final Random rand = new Random();

    public GameManager() {
        this.players = new ArrayList<>();
        this.state = GameState.WAITING_FOR_PLAYERS;
        this.currentPlayerIndex = 0;
        this.diceLogic = new DiceLogic();
    }

    public void addPlayer(Player player) {
        if (state == GameState.WAITING_FOR_PLAYERS) {
            players.add(player);
        }
    }

    public void startGame() {
        if (players.size() >= 2 && players.size() <= 4) {
            this.state = GameState.STARTING_ROUND;
            startRound();
        }
    }

    private void startRound() {
        this.currentBid = null;
        this.lastBidder = null;
        this.skipNextTurn = false;
        this.hasPlayedCardThisTurn = false; // Reset al empezar ronda

        for (Player p : players) {
            if (!p.isEliminated()) {
                for (Die die : p.cup()) {
                    die.roll();
                }
            }
        }
        ensureValidTurn();

        this.state = GameState.PLAYER_TURN;
    }

    private void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).isEliminated());

        if (skipNextTurn) {
            skipNextTurn = false;
            nextTurn();
        }
        this.hasPlayedCardThisTurn = false; // Reset al pasar el turno a otro
    }

    private void ensureValidTurn() {
        if (players.get(currentPlayerIndex).isEliminated()) {
            nextTurn();
        }
    }

    public boolean placeBid(String playerId, Bid bid) {
        if (state != GameState.PLAYER_TURN)
            return false;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getId().equals(playerId))
            return false;

        if (diceLogic.isValid(bid, currentBid, getTotalDiceCount())) {
            this.currentBid = bid;
            this.lastBidder = currentPlayer;
            nextTurn();
            return true;
        }

        return false;
    }

    public void dealCards(CardRegistry registry) {
        List<Card> allTriunfos = new ArrayList<>();
        List<Card> allPalos = new ArrayList<>();
        List<Card> allCards = new ArrayList<>();

        for (int i = 1; i <= 40; i++) {
            Card c = registry.getCardById(i);
            if (c != null) {
                allCards.add(c);
                if (c.type() == CardType.TRIUNFO)
                    allTriunfos.add(c);
                else if (c.type() == CardType.PALO)
                    allPalos.add(c);
            }
        }

        for (Player p : players) {
            // Si el jugador ya tiene cartas en mano (las que pusimos en START_GAME desde el Lobby), NO las sobreescribimos
            if (!p.hand().isEmpty()) continue;

            if (!allTriunfos.isEmpty())
                p.hand().add(allTriunfos.get(rand.nextInt(allTriunfos.size())));
            if (!allPalos.isEmpty())
                p.hand().add(allPalos.get(rand.nextInt(allPalos.size())));
            if (!allCards.isEmpty())
                p.hand().add(allCards.get(rand.nextInt(allCards.size())));
        }
    }

    public boolean playCard(String playerId, int cardId, String targetPlayerId) {
        if (state != GameState.PLAYER_TURN)
            return false;
        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getId().equals(playerId))
            return false;

        if (hasPlayedCardThisTurn)
            return false;

        Card cardToPlay = null;
        for (Card c : currentPlayer.hand()) {
            if (c.id() == cardId) {
                cardToPlay = c;
                break;
            }
        }
        if (cardToPlay == null)
            return false;

        currentPlayer.hand().remove(cardToPlay);
        hasPlayedCardThisTurn = true; // Marcamos que ya ha usado su carta de este turno

        com.TFG1.core.cards.effects.CardEffectStrategy strategy = com.TFG1.core.cards.effects.CardEffectFactory.getStrategy(cardToPlay);
        if (strategy != null) {
            strategy.applyEffect(this, currentPlayer, cardToPlay, targetPlayerId);
        }

        if (cardToPlay.type() == CardType.TRIUNFO) {
            skipNextTurn = true;
        }

        checkGameOver();

        return true;
    }

    public String callDoubt(String playerId) {
        if (state != GameState.PLAYER_TURN || currentBid == null)
            return null;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getId().equals(playerId))
            return null;

        this.state = GameState.RESOLVING_DOUBT;
        return resolveDoubt(currentPlayer);
    }

    private String resolveDoubt(Player doubter) {
        int specificFace = currentBid.value();
        int expectedQuantity = currentBid.quantity();

        int actualQuantity = 0;

        for (Player p : players) {
            if (!p.isEliminated()) {
                for (Die die : p.cup()) {
                    int rolledValue = die.getValue();
                    if (rolledValue == specificFace || rolledValue == 1) {
                        actualQuantity++;
                    }
                }
            }
        }

        String result;
        if (actualQuantity >= expectedQuantity) {

            doubter.loseDie();
            currentPlayerIndex = players.indexOf(doubter);
            result = "¡Fallo de " + doubter.getName() + "! Sí había al menos " + expectedQuantity + " dados de " + specificFace + " (Había " + actualQuantity + "). Pierde un dado.";
        } else {

            lastBidder.loseDie();
            currentPlayerIndex = players.indexOf(lastBidder);
            result = "¡" + doubter.getName() + " acertó! No había " + expectedQuantity + " dados de " + specificFace + " (Solo había " + actualQuantity + "). " + lastBidder.getName() + " pierde un dado.";
        }

        checkGameOver();
        return result;
    }

    private void checkGameOver() {
        int activePlayers = 0;
        for (Player p : players) {
            if (!p.isEliminated()) {
                activePlayers++;
            }
        }

        if (activePlayers <= 1) {
            this.state = GameState.GAME_OVER;
        } else {
            startRound();
        }
    }

    public void handleDisconnect(String playerId) {
        for (Player p : players) {
            if (p.getId().equals(playerId)) {
                p.eliminateFull();

                if (state == GameState.PLAYER_TURN && players.indexOf(p) == currentPlayerIndex) {
                    nextTurn();
                }

                checkGameOver();
                break;
            }
        }
    }

    private int getTotalDiceCount() {
        int count = 0;
        for (Player p : players) {
            if (!p.isEliminated()) {
                count += p.cup().size();
            }
        }
        return count;
    }

    public GameState getState() {
        return state;
    }

    public Bid getCurrentBid() {
        return currentBid;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getWinner() {
        if (state != GameState.GAME_OVER)
            return null;
        for (Player p : players) {
            if (!p.isEliminated()) {
                return p;
            }
        }
        return null;
    }

    public List<Die> getAllDiceOnTable() {
        List<Die> all = new ArrayList<>();
        for (Player p : players) {
            if (!p.isEliminated()) {
                all.addAll(p.cup());
            }
        }
        return all;
    }

    public void rotateDiceRight() {
        if (players.size() < 2) return;
        List<Player> activePlayers = new ArrayList<>();
        for (Player p : players) {
            if (!p.isEliminated()) activePlayers.add(p);
        }
        if (activePlayers.size() < 2) return;

        List<List<Die>> cupsCopy = new ArrayList<>();
        for (Player p : activePlayers) {
            cupsCopy.add(new ArrayList<>(p.cup()));
        }

        for (int i = 0; i < activePlayers.size(); i++) {
            Player current = activePlayers.get(i);
            int prevIndex = (i - 1 + activePlayers.size()) % activePlayers.size();
            List<Die> prevCup = cupsCopy.get(prevIndex);

            current.cup().clear();
            current.cup().addAll(prevCup);
        }
    }

    public void rerollTable() {
        for (Player p : players) {
            if (!p.isEliminated()) {
                for (Die d : p.cup()) {
                    d.roll();
                }
            }
        }
    }

    public Player getPlayerById(String id) {
        for (Player p : players) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public void revealDie(Player target, Die die) {

        System.out.println("REVEAL: El jugador " + target.getName() + " tiene un dado con valor " + die.getValue());
    }

    public void markBlindDuelActive() {
        System.out.println("Duelo a ciegas activado.");
    }
}
