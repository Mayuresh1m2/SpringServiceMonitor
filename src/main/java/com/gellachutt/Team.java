package com.gellachutt;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private List<Player> players;

    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getAlivePlayers() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.isAlive()) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }

    public String getName() {
        return name;
    }
}
