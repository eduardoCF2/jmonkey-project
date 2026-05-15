package com.TFG1.service;

import com.TFG1.core.engine.GameManager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors player connections and triggers automatic forfeits in the
 * GameManager
 * if a player fails to ping within the specified timeout
 */
public class HeartbeatService {

    private final ConcurrentHashMap<String, Long> lastHeartbeats;
    private boolean isRunning;
    private Thread monitorThread;

    public HeartbeatService() {
        this.lastHeartbeats = new ConcurrentHashMap<>();
        this.isRunning = false;
    }

    /**
     * Registers a player or updates their last seen time
     *
     * @param playerId The ID of the player pinging the server
     */
    public void ping(String playerId) {
        lastHeartbeats.put(playerId, System.currentTimeMillis());
    }

    /**
     * Removes a player from the heartbeat tracking entirely (eg they officially
     * left)
     */
    public void removePlayer(String playerId) {
        lastHeartbeats.remove(playerId);
    }

    /**
     * Starts the monitoring thread in the background
     *
     * @param gm        The actively running GameManager
     * @param timeoutMs The maximum allowed time (in ms) without a ping before
     *                  disconnection is assumed
     */
    public void startMonitoring(GameManager gm, long timeoutMs) {
        if (isRunning)
            return;

        isRunning = true;
        monitorThread = new Thread(() -> {
            while (isRunning) {
                long now = System.currentTimeMillis();

                for (String playerId : lastHeartbeats.keySet()) {
                    long lastSeen = lastHeartbeats.get(playerId);

                    if (now - lastSeen > timeoutMs) {
                        System.out.println("[HeartbeatService] Player ID " + playerId
                                + " ha excedido el timeout de conexion. Desconectando.");

                        lastHeartbeats.remove(playerId);

                        gm.handleDisconnect(playerId);
                    }
                }

                try {

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * Stops the background monitor
     */
    public void stopMonitoring() {
        isRunning = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }
}
