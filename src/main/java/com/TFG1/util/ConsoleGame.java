package com.TFG1.util;

import com.TFG1.core.dice.Bid;
import com.TFG1.core.dice.Die;
import com.TFG1.core.engine.GameManager;
import com.TFG1.core.engine.GameState;
import com.TFG1.core.engine.Player;

import java.util.Scanner;

public class ConsoleGame {

    public static void main(String[] args) {
        GameManager gm = new GameManager();

        Player p1 = new Player("1", "Edu", 5);
        Player p2 = new Player("2", "Sanz", 5);
        Player p3 = new Player("3", "Soler", 5);

        gm.addPlayer(p1);
        gm.addPlayer(p2);
        gm.addPlayer(p3);
        gm.startGame();
        Scanner scanner = new Scanner(System.in);

        int roundNumber = 1;
        while (gm.getState() != GameState.GAME_OVER) {

            Player currentPlayer = gm.getCurrentPlayer();

            System.out.println("\n--- TURNO DE " + currentPlayer.getName().toUpperCase() + " ---");

            Bid currentBid = gm.getCurrentBid();
            if (currentBid == null) {
                System.out.println("Mesa vacia. Eres el primero en apostar");
            } else {
                System.out.println("Apuesta actual en la mesa: " + currentBid.quantity() + " dados con el valor de "
                        + currentBid.value());
            }

            System.out.print("Tus dados ocultos son: [");
            for (int i = 0; i < currentPlayer.cup().size(); i++) {
                System.out.print(currentPlayer.cup().get(i).getValue());
                if (i < currentPlayer.cup().size() - 1)
                    System.out.print(", ");
            }
            System.out.println("]");

            System.out.println("Que quieres hacer?");
            System.out.println(" -> Escribe 'bid <cantidad> <valor>' para apostar (ej: bid 2 4)");
            if (currentBid != null) {
                System.out.println(" -> Escribe 'doubt' para dudar de la apuesta actual");
            }

            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.startsWith("bid ")) {
                try {
                    String[] parts = input.split(" ");
                    int qty = Integer.parseInt(parts[1]);
                    int val = Integer.parseInt(parts[2]);

                    if (val < 1 || val > 6) {
                        System.out.println("ERROR: El valor del dado debe ser entre 1 y 6");
                        continue;
                    }

                    Bid newBid = new Bid(qty, val);
                    boolean success = gm.placeBid(currentPlayer.getId(), newBid);

                    if (!success) {
                        System.out.println(
                                "ERROR: Jugada invalida. La apuesta debe incrementar la cantidad de dados o el valor facial de la apuesta actual.");
                    } else {
                        System.out.println(
                                "[+] " + currentPlayer.getName() + " aposto " + qty + " dados con valor " + val);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: Formato incorrecto. Usa: bid <cantidad> <valor>");
                }
            } else if (input.equals("doubt")) {
                if (currentBid == null) {
                    System.out.println("ERROR: No puedes dudar porque no hay ninguna apuesta en la mesa");
                    continue;
                }

                System.out.println("\n[!] " + currentPlayer.getName() + " HA DUDADO!");
                System.out.println("Vamos a contar los dados de todos...");

                Player[] players = { p1, p2, p3 };
                int actualCount = 0;
                for (Player p : players) {
                    if (!p.isEliminated()) {
                        System.out.print(p.getName() + " tenia: [");
                        for (int i = 0; i < p.cup().size(); i++) {
                            int val = p.cup().get(i).getValue();
                            System.out.print(val);
                            if (i < p.cup().size() - 1)
                                System.out.print(", ");
                            if (val == currentBid.value() || val == 1) {
                                actualCount++;
                            }
                        }
                        System.out.println("]");
                    }
                }

                System.out.println("-> Total de dados validos (incluyendo comodines '1'): " + actualCount);
                if (actualCount >= currentBid.quantity()) {
                    System.out.println("-> La apuesta era VERDAD! El que dudo pierde un dado.");
                } else {
                    System.out.println("-> La apuesta era MENTIRA! El mentiroso pierde un dado.");
                }

                gm.callDoubt(currentPlayer.getId());
                System.out.println("--------------------------------------");

                for (Player p : players) {
                    System.out.println(p.getName() + " tiene " + p.cup().size() + " dados.");
                }

                System.out.println("Presiona ENTER para empezar la siguiente ronda...");
                scanner.nextLine();
            } else {
                System.out.println("ERROR: Comando no reconocido");
            }
        }

        System.out.println("\n======================================");
        System.out.println("          PARTIDA TERMINADA           ");
        System.out.println("GANADOR: " + gm.getCurrentPlayer().getName());
        System.out.println("======================================");
        scanner.close();
    }
}
