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

        // Roll all dice for each active player
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
    }

    private void ensureValidTurn() {
        if (players.get(currentPlayerIndex).isEliminated()) {
            nextTurn();
        }
    }

    // --- GAMEPLAY ACTIONS ---

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
            p.hand().clear();
            if (!allTriunfos.isEmpty())
                p.hand().add(allTriunfos.get(rand.nextInt(allTriunfos.size())));
            if (!allPalos.isEmpty())
                p.hand().add(allPalos.get(rand.nextInt(allPalos.size())));
            if (!allCards.isEmpty())
                p.hand().add(allCards.get(rand.nextInt(allCards.size())));
        }
    }

    public boolean playCard(String playerId, int cardId) {
        if (state != GameState.PLAYER_TURN)
            return false;
        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getId().equals(playerId))
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

        if (cardToPlay.type() == CardType.PALO) {
            for (Die d : currentPlayer.cup())
                d.roll();
        } else if (cardToPlay.type() == CardType.TRIUNFO) {
            skipNextTurn = true;
        } else if (cardToPlay.type() == CardType.JOKER) {
            int targetIdx = currentPlayerIndex;
            do {
                targetIdx = (targetIdx + 1) % players.size();
            } while (players.get(targetIdx).isEliminated());

            Player target = players.get(targetIdx);
            target.loseDie();

            int activePlayers = 0;
            for (Player p : players)
                if (!p.isEliminated())
                    activePlayers++;
            if (activePlayers <= 1)
                this.state = GameState.GAME_OVER;
        }
        return true;
    }

    public boolean callDoubt(String playerId) {
        if (state != GameState.PLAYER_TURN || currentBid == null)
            return false;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getId().equals(playerId))
            return false;

        this.state = GameState.RESOLVING_DOUBT;
        resolveDoubt(currentPlayer);
        return true;
    }

    private void resolveDoubt(Player doubter) {
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

        if (actualQuantity >= expectedQuantity) {
            // The bid was true! Doubter loses a die.
            doubter.loseDie();
            currentPlayerIndex = players.indexOf(doubter); // Loser starts next round
        } else {
            // The bid was false! Bidder loses a die.
            lastBidder.loseDie();
            currentPlayerIndex = players.indexOf(lastBidder); // Loser starts next round
        }

        checkGameOver();
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

    // --- DISCONNECT ---

    public void handleDisconnect(String playerId) {
        for (Player p : players) {
            if (p.getId().equals(playerId)) {
                p.eliminateFull(); // Instantly eliminate their cup

                // If it was their turn, immediately skip it
                if (state == GameState.PLAYER_TURN && players.indexOf(p) == currentPlayerIndex) {
                    nextTurn();
                }

                // Make sure we didn't just end the game
                checkGameOver();
                break;
            }
        }
    }

    // --- HELPERS ---

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
                return p; // The last one standing
            }
        }
        return null;
    }
}
