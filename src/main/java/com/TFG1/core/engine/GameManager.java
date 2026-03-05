package com.TFG1.core.engine;

import com.TFG1.core.dice.Bid;
import com.TFG1.core.dice.DiceLogic;
import com.TFG1.core.dice.Die;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private final List<Player> players;
    private GameState state;
    private int currentPlayerIndex;
    private Bid currentBid;
    private Player lastBidder;
    private final DiceLogic diceLogic;

    public GameManager() {
        this.players = new ArrayList<>();
        this.state = GameState.WAITING_FOR_PLAYERS;
        this.currentPlayerIndex = 0;
        this.diceLogic = new DiceLogic();
    }

    // --- INITIALIZATION ---

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

    // --- ROUND MANAGEMENT ---

    private void startRound() {
        this.currentBid = null;
        this.lastBidder = null;

        // Roll all dice for each active player
        for (Player p : players) {
            if (!p.isEliminated()) {
                for (Die die : p.cup()) {
                    die.roll();
                }
            }
        }

        // Shift starting player index logic can go here (to whoever lost last)
        ensureValidTurn();

        this.state = GameState.PLAYER_TURN;
    }

    private void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).isEliminated());
    }

    private void ensureValidTurn() {
        if (players.get(currentPlayerIndex).isEliminated()) {
            nextTurn();
        }
    }

    // --- GAMEPLAY ACTIONS ---

    public boolean placeBid(int playerId, Bid bid) {
        if (state != GameState.PLAYER_TURN)
            return false;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getId() != playerId)
            return false;

        if (diceLogic.isValid(bid, currentBid, getTotalDiceCount())) {
            this.currentBid = bid;
            this.lastBidder = currentPlayer;
            nextTurn();
            return true;
        }

        return false;
    }

    public boolean callDoubt(int playerId) {
        if (state != GameState.PLAYER_TURN || currentBid == null)
            return false;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getId() != playerId)
            return false;

        this.state = GameState.RESOLVING_DOUBT;
        resolveDoubt(currentPlayer);
        return true;
    }

    // --- RESOLUTION ---

    private void resolveDoubt(Player doubter) {
        int specificFace = currentBid.value();
        int expectedQuantity = currentBid.quantity();

        int actualQuantity = 0;

        for (Player p : players) {
            if (!p.isEliminated()) {
                for (Die die : p.cup()) {
                    int rolledValue = die.getValue();
                    // Ones map as jokers in traditional Perudo, unless a palifico round, but
                    // sticking to basic logic for now.
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

    public void handleDisconnect(int playerId) {
        for (Player p : players) {
            if (p.getId() == playerId) {
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
}
