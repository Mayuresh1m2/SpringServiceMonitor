package com.gellachutt;

public class Zone {
    public final int x1;
    public final int y1;
    public final int x2;
    public final int y2;

    public Zone(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean isInside(Player player) {
        return player.getX() >= x1 && player.getX() <= x2 &&
               player.getY() >= y1 && player.getY() <= y2;
    }
}
