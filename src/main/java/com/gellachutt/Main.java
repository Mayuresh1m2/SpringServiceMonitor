package com.gellachutt;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Game game = new Game();
        int round = 0;

        while (!game.isGameOver() && round < 200) {
            System.out.println("Round: " + (round + 1));
            game.update();
            game.printState();
            Thread.sleep(100); // Pause for 100ms to make the simulation viewable
            round++;
        }

        if (round >= 200) {
            System.out.println("Game timed out after 200 rounds.");
        }
    }
}
