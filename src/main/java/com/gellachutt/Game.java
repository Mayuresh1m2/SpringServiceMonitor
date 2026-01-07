package com.gellachutt;

public class Game {
    private final GameMap map;
    private final Team inGroup;
    private final Team outGroup;
    private Player king;
    private boolean isGameOver;

    // Game constants
    private static final int MAP_WIDTH = 200;
    private static final int MAP_HEIGHT = 100;
    private static final int PRISON_X1 = 0;
    private static final int PRISON_Y1 = 0;
    private static final int PRISON_X2 = 10;
    private static final int PRISON_Y2 = 10;
    private static final int HOME_X1 = 190;
    private static final int HOME_Y1 = 90;
    private static final int HOME_X2 = 200;
    private static final int HOME_Y2 = 100;
    private static final int KING_START_X = 5;
    private static final int KING_START_Y = 5;
    private static final int IN_GROUP_DEFENDERS = 6;
    private static final int DEFENDER_START_X = 195;
    private static final int DEFENDER_START_Y = 95;
    private static final int OUT_GROUP_HUNTERS = 7;
    private static final int HUNTER_START_X = 100;
    private static final int HUNTER_START_Y = 50;
    private static final int TAG_RADIUS = 5;
    private static final int DISPLAY_SCALE = 10;

    public Game() {
        Zone prison = new Zone(PRISON_X1, PRISON_Y1, PRISON_X2, PRISON_Y2);
        Zone home = new Zone(HOME_X1, HOME_Y1, HOME_X2, HOME_Y2);
        this.map = new GameMap(MAP_WIDTH, MAP_HEIGHT, prison, home);

        this.inGroup = new Team("In-group");
        this.outGroup = new Team("Out-group");

        // Initialize players
        this.king = new Player(KING_START_X, KING_START_Y, inGroup, true);
        this.inGroup.addPlayer(king);
        for (int i = 0; i < IN_GROUP_DEFENDERS; i++) {
            this.inGroup.addPlayer(new Player(DEFENDER_START_X, DEFENDER_START_Y, inGroup, false));
        }

        for (int i = 0; i < OUT_GROUP_HUNTERS; i++) {
            this.outGroup.addPlayer(new Player(HUNTER_START_X, HUNTER_START_Y, outGroup, false));
        }

        this.isGameOver = false;
    }

    public void update() {
        if (isGameOver) {
            return;
        }

        // 1. King's movement (simple AI: move towards home)
        if (!map.getHome().isInside(king)) {
            int homeCenterX = (map.getHome().x1 + map.getHome().x2) / 2;
            int homeCenterY = (map.getHome().y1 + map.getHome().y2) / 2;
            int dx = Integer.signum(homeCenterX - king.getX());
            int dy = Integer.signum(homeCenterY - king.getY());
            king.move(king.getX() + dx, king.getY() + dy);
        }

        // 2. Out-group's movement (simple AI: move towards king)
        for (Player hunter : outGroup.getAlivePlayers()) {
            int dx = Integer.signum(king.getX() - hunter.getX());
            int dy = Integer.signum(king.getY() - hunter.getY());
            hunter.move(hunter.getX() + dx, hunter.getY() + dy);
        }

        // 3. In-group's movement (simple AI: move towards nearest hunter)
        for (Player defender : inGroup.getAlivePlayers()) {
            if (defender.isKing()) continue;

            Player nearestHunter = findNearestOpponent(defender, outGroup);
            if (nearestHunter != null) {
                int dx = Integer.signum(nearestHunter.getX() - defender.getX());
                int dy = Integer.signum(nearestHunter.getY() - defender.getY());
                defender.move(defender.getX() + dx, defender.getY() + dy);
            }
        }

        // 4. Tagging logic
        for (Player hunter : outGroup.getAlivePlayers()) {
            // Check if king is tagged
            if (hunter.distanceTo(king) < TAG_RADIUS) {
                endRound("Out-group wins! King was tagged.");
                return;
            }
            // Check if hunter is tagged by a defender
            for (Player defender : inGroup.getAlivePlayers()) {
                 if (!defender.isKing() && hunter.distanceTo(defender) < TAG_RADIUS) {
                    hunter.setAlive(false);
                }
            }
        }

        // 5. Win conditions
        if (map.getHome().isInside(king)) {
            endRound("In-group wins! The king reached home.");
            return;
        }

        if (outGroup.getAlivePlayers().isEmpty()) {
            endRound("In-group wins! All hunters have been tagged.");
            return;
        }
    }

    private Player findNearestOpponent(Player player, Team opponents) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Player opponent : opponents.getAlivePlayers()) {
            double distance = player.distanceTo(opponent);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = opponent;
            }
        }
        return nearest;
    }

    private void endRound(String message) {
        System.out.println(message);
        this.isGameOver = true;
    }

    public void printState() {
        int scale = 10; // Scale down the map for display
        int displayWidth = map.getWidth() / scale;
        int displayHeight = map.getHeight() / scale;
        char[][] grid = new char[displayHeight][displayWidth];

        // Initialize grid with empty space
        for (int i = 0; i < displayHeight; i++) {
            for (int j = 0; j < displayWidth; j++) {
                grid[i][j] = '.';
            }
        }

        // Mark players on the grid
        for (Player player : inGroup.getPlayers()) {
            if (player.isAlive()) {
                int x = player.getX() / scale;
                int y = player.getY() / scale;
                if (x >= 0 && x < displayWidth && y >= 0 && y < displayHeight) {
                    grid[y][x] = player.isKing() ? 'K' : 'D';
                }
            }
        }
        for (Player player : outGroup.getPlayers()) {
            if (player.isAlive()) {
                int x = player.getX() / scale;
                int y = player.getY() / scale;
                if (x >= 0 && x < displayWidth && y >= 0 && y < displayHeight) {
                    grid[y][x] = 'H';
                }
            }
        }

        // Print grid
        for (int i = 0; i < displayHeight; i++) {
            for (int j = 0; j < displayWidth; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("K: King, D: Defender, H: Hunter");
        System.out.println("----------------------------------------");
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}
