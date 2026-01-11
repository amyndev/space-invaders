// GameManager.java
package org.example.game;

import java.util.ArrayList;
import java.util.Random;

// Gestionnaire principal du jeu: score, niveau, et logique des aliens
public class GameManager {
    // Fenêtre de temps pour les combos (1 seconde)
    private static final long COMBO_TIMEOUT = 1_000_000_000; // 1 second in nanoseconds

    private static double alienVelocityX = 2;
    private int score = 0;
    private int highScore = 0;
    private int level = 1;
    private int alienCount = 0;
    private int alienRows = 2;
    private int alienColumns = 3;
    private double alienVelocityMultiplier = 1.0;
    private int currentCombo = 0;
    private long lastKillTime = 0;

    private final ArrayList<Alien> aliens;
    private final ArrayList<Bullet> alienBullets;

    public GameManager(ArrayList<Alien> aliens, ArrayList<Bullet> alienBullets) {
        this.aliens = aliens;
        this.alienBullets = alienBullets;
    }

    public void updateAliens(Ship ship) {
        boolean needsDirectionChange = false;

        for (Alien alien : aliens) {
            if (alien.isAlive()) {
                if (alien.x + alien.width >= GameConfig.BOARD_WIDTH || alien.x <= 0) {
                    needsDirectionChange = true;
                }
            }
        }

        if (needsDirectionChange) {
            alienVelocityX *= -1;
            aliens.forEach(Alien::drop);
        }
    }

    public boolean checkAliensReachedBottom() {
        for (Alien alien : aliens) {
            if (alien.isAlive()) {
                // Check if alien has reached ship's vertical position
                if (alien.y + alien.height >= GameConfig.BOARD_HEIGHT - GameConfig.TILE_SIZE * 3) {
                    return true;
                }
            }
        }
        return false;
    }

    public void alienShoot() {
        Random random = new Random();
        ArrayList<Alien> bottomAliens = getBottomAliens();
        
        // Calculate shooting probability based on level
        double shootProbability = GameConfig.BASE_ALIEN_SHOOT_PROBABILITY + 
            (level - 1) * GameConfig.ALIEN_SHOOT_PROBABILITY_INCREMENT;
        
        // Cap the maximum probability at 0.03 to prevent overwhelming the player
        shootProbability = Math.min(shootProbability, 0.03);
        
        for (Alien alien : bottomAliens) {
            if (alien.isAlive() && random.nextDouble() < shootProbability) {
                AlienType type = alien.getType();
                alienBullets.add(new Bullet(
                    alien.x + alien.width / 2,
                    alien.y + alien.height,
                    GameConfig.TILE_SIZE / 6,
                    GameConfig.TILE_SIZE / 2,
                    5 + level * 0.5,
                    type.getFireColor()
                ));
            }
        }
    }

    private ArrayList<Alien> getBottomAliens() {
        ArrayList<Alien> bottomAliens = new ArrayList<>();
        double[] columnPositions = new double[alienColumns];
        Alien[] bottomMostAliens = new Alien[alienColumns];

        // Initialize column positions based on initial layout
        for (int i = 0; i < alienColumns; i++) {
            columnPositions[i] = calculateInitialX(alienColumns) + i * (GameConfig.TILE_SIZE * 3) + GameConfig.TILE_SIZE;
        }

        // Find bottom-most alien in each column
        for (Alien alien : aliens) {
            if (alien.isAlive()) {
                // Find which column this alien belongs to
                for (int i = 0; i < alienColumns; i++) {
                    if (Math.abs(alien.x + alien.width/2 - columnPositions[i]) < GameConfig.TILE_SIZE) {
                        // If this alien is lower than the current bottom-most alien in this column
                        if (bottomMostAliens[i] == null || alien.y > bottomMostAliens[i].y) {
                            bottomMostAliens[i] = alien;
                        }
                        break;
                    }
                }
            }
        }

        // Add all found bottom aliens to the result list
        for (Alien alien : bottomMostAliens) {
            if (alien != null) {
                bottomAliens.add(alien);
            }
        }
        
        return bottomAliens;
    }

    public boolean checkNextLevel(Ship ship) {  // Add Ship parameter
        if (alienCount == 0) {
            level++;
            score += alienColumns * alienRows * 100 * level;
            checkShipUpgrade(ship);
            // Calculate max columns and rows based on board size
            int maxColumns = (GameConfig.BOARD_WIDTH / (GameConfig.TILE_SIZE * 3)) - 1;
            int maxRows = (GameConfig.BOARD_HEIGHT / (GameConfig.TILE_SIZE * 2)) - 4;
            
            alienColumns = Math.min(alienColumns + 1, maxColumns);
            alienRows = Math.min(alienRows + 1, maxRows);
            alienVelocityMultiplier *= 1.2;
            
            // Play level up sound
            SoundManager.playLevelUp();
            return true;
        }
        return false;
    }

    public void decrementAlienCount() {
        alienCount--;
    }

    public void reset() {
        score = 0;
        level = 1;
        alienCount = 0;
        alienVelocityMultiplier = 1.0;
        alienRows = 2;
        alienColumns = 3;
        alienVelocityX = 2;
        currentCombo = 0;
        lastKillTime = 0;
        
        // Clear collections
        aliens.clear();
        alienBullets.clear();
    }

    // Getters and setters
    public static double getAlienVelocityX() {
        return alienVelocityX;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public int getLevel() {
        return level;
    }

    public int getAlienRows() {
        return alienRows;
    }

    public int getAlienColumns() {
        return alienColumns;
    }

    public double getAlienVelocityMultiplier() {
        return alienVelocityMultiplier;
    }

    public void addScore(int points) {
        long currentTime = System.nanoTime();
        if (currentTime - lastKillTime < COMBO_TIMEOUT) {
            currentCombo++;
        } else {
            currentCombo = 1;
        }
        lastKillTime = currentTime;
        score += points;
        if (score > highScore) {
            highScore = score;
        }
    }

    public int getCurrentCombo() {
        long currentTime = System.nanoTime();
        if (currentTime - lastKillTime > COMBO_TIMEOUT) {
            currentCombo = 0;
        }
        return currentCombo;
    }

    public void setAlienCount(int count) {
        this.alienCount = count;
    }

    public int calculateInitialX(int columns) {
        int totalAliensWidth = columns * (GameConfig.TILE_SIZE * 3);
        return (GameConfig.BOARD_WIDTH - totalAliensWidth) / 2;
    }
    
    public AlienType getCurrentLevelAlienType() {
        if (level <= 4) return AlienType.SPECTER;
        if (level <= 8) return AlienType.TOXIN;
        if (level <= 12) return AlienType.PHANTOM;
        return AlienType.CRUSHER;
    }

    public LevelStructure getCurrentLevelStructure() {
        switch (level) {
            case 1:  // Introductory Wave
                return new LevelStructure(new AlienType[][]{
                    {AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN},
                    {AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER}
                });
            case 2:  // Mixed Challenge
                return new LevelStructure(new AlienType[][]{
                    {AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM},
                    {AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN},
                    {AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER}
                });
            case 3:  // Increasing Difficulty
                return new LevelStructure(new AlienType[][]{
                    {AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER},
                    {AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM},
                    {AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN},
                    {AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER}
                });
            case 4:  // Alternating Rows
                return new LevelStructure(new AlienType[][]{
                    {AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER},
                    {AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM},
                    {AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN},
                    {AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER}
                });
            case 5:  // Advanced Challenge
                return new LevelStructure(new AlienType[][]{
                    {AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER, AlienType.CRUSHER},
                    {AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM, AlienType.PHANTOM},
                    {AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN, AlienType.TOXIN},
                    {AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER, AlienType.SPECTER}
                });
            default:
                // For levels beyond 5, create a randomized challenging pattern
                return generateRandomLevelStructure();
        }
    }

    // Génère une structure de niveau aléatoire pour les niveaux avancés
    private LevelStructure generateRandomLevelStructure() {
        int rows = Math.min(6, level / 2 + 3);
        int cols = Math.min(10, level / 2 + 5);
        AlienType[][] layout = new AlienType[rows][cols];
        
        AlienType[] types = AlienType.values();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Higher levels have more chance of tougher aliens
                int typeIndex = Math.min(types.length - 1, 
                    (int)(Math.random() * (level / 3.0)));
                layout[i][j] = types[typeIndex];
            }
        }
        return new LevelStructure(layout);
    }

    private void checkShipUpgrade(Ship ship) {
        if (level >= 5 && score >= 10000) {
            ship.setShipType(ShipType.BLAZE);
        } else if (level >= 4 && score >= 5000) {
            ship.setShipType(ShipType.FURY);
        } else if (level >= 3 && score >= 3000) {
            ship.setShipType(ShipType.SPECTRA);
        }
    }
}