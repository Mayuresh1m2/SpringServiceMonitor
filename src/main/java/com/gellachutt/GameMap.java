package com.gellachutt;

public class GameMap {
    private final int width;
    private final int height;
    private final Zone prison;
    private final Zone home;

    public GameMap(int width, int height, Zone prison, Zone home) {
        this.width = width;
        this.height = height;
        this.prison = prison;
        this.home = home;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Zone getPrison() {
        return prison;
    }

    public Zone getHome() {
        return home;
    }
}
