package com.gellachutt;

public class Player {
    private int x;
    private int y;
    private Team team;
    private boolean isKing;
    private boolean isAlive;

    public Player(int x, int y, Team team, boolean isKing) {
        this.x = x;
        this.y = y;
        this.team = team;
        this.isKing = isKing;
        this.isAlive = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isKing() {
        return isKing;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public void move(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public double distanceTo(Player other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }
}
