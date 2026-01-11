// Ship.java
package org.example.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

// Vaisseau du joueur: contrôles et mouvement
public class Ship extends GameObject {
    // Temps entre les tirs (0.1 seconde)
    private static final long SHOOT_COOLDOWN = 100_000_000; // Reduced to 0.1 seconds
    private static final int VELOCITY = 1;
    private static final double ACCELERATION = 0.5;
    private static final double MAX_VELOCITY = 8.0;
    private static final double DECELERATION = 0.3;
    private static final long CLICK_SPEED_WINDOW = 1_000_000_000L; // 1 second window
    private static final int MAX_CLICK_SPEED = 20;
    private static final int MAX_QUEUED_SHOTS = 5; // Maximum number of queued shots
    private final Set<KeyCode> activeKeys;
    private final ArrayList<Bullet> playerBullets;
    private long lastShotTime = 0;
    private boolean canShoot = true;
    private int lives = 10;
    private ShipType type;
    private double velocityX = 0;
    private boolean useMouseControl = false;
    private long[] recentClickTimes = new long[5]; // Store last 5 click times
    private int clickIndex = 0;
    private double currentBulletSpeed = 5.0;
    private Queue<Long> shootQueue = new LinkedList<>();
    private boolean spaceWasPressed = false; // Add this field

    public Ship(double x, double y, int width, int height, Image shipImg, Set<KeyCode> activeKeys, ArrayList<Bullet> playerBullets) {
        super(x, y, width, height, null);
        this.activeKeys = activeKeys;
        this.playerBullets = playerBullets;
        this.lives = 10;
        setShipType(ShipType.ALPHA);
        this.useMouseControl = false;
    }

    public int getLives() {
        return lives;
    }

    public void decrementLives() {
        lives--;
        if (lives > 0) {
            startHitAnimation();
        }
    }

    public void setShipType(ShipType newType) {
        // Only upgrade if new type is better than current
        if (this.type == null || newType.ordinal() > this.type.ordinal()) {
            this.type = newType;
            this.img = new Image(getClass().getResourceAsStream("/" + type.getImagePath()));
        }
    }

    public void upgradeShip() {
        if (type.ordinal() < ShipType.values().length - 1) {
            setShipType(ShipType.values()[type.ordinal() + 1]);
        }
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setMouseControl(boolean useMouseControl) {
        this.useMouseControl = useMouseControl;
    }

    public void updateMousePosition(double mouseX) {
        if (!useMouseControl) return;
        
        // Calculate target position (center ship on mouse)
        double targetX = mouseX - width / 2;
        
        // Implement smoother movement
        double diff = targetX - x;
        double maxSpeed = MAX_VELOCITY;
        double acceleration = Math.min(Math.abs(diff) * 0.2, maxSpeed);
        
        if (Math.abs(diff) < 1.0) {
            velocityX = 0;
            x = targetX;
        } else {
            velocityX = diff > 0 ? acceleration : -acceleration;
            x += velocityX;
        }
        
        // Keep ship within bounds
        x = Math.max(0, Math.min(GameConfig.BOARD_WIDTH - width, x));
    }

    @Override
    void update() {
        updateHitAnimation();

        if (!useMouseControl) {
            // Keyboard movement
            boolean movingLeft = activeKeys.contains(KeyCode.LEFT);
            boolean movingRight = activeKeys.contains(KeyCode.RIGHT);
            
            if (movingLeft && !movingRight) {
                velocityX = Math.max(-MAX_VELOCITY, velocityX - ACCELERATION);
            } else if (movingRight && !movingLeft) {
                velocityX = Math.min(MAX_VELOCITY, velocityX + ACCELERATION);
            } else {
                // Apply deceleration when no keys are pressed
                velocityX *= 0.8; // Smooth deceleration
                if (Math.abs(velocityX) < 0.1) velocityX = 0;
            }
            
            x += velocityX;
            x = Math.max(0, Math.min(GameConfig.BOARD_WIDTH - width, x));

            // Modified shooting logic
            boolean spaceCurrentlyPressed = activeKeys.contains(KeyCode.SPACE);
            if (spaceCurrentlyPressed && !spaceWasPressed) {
                shoot();
            }
            spaceWasPressed = spaceCurrentlyPressed;
        }
    }

    public void shoot() {
        long currentTime = System.nanoTime();
        
        // Clean up old queued shots
        while (!shootQueue.isEmpty() && shootQueue.peek() + SHOOT_COOLDOWN < currentTime) {
            shootQueue.poll();
        }

        // If queue is full, ignore new shots
        if (shootQueue.size() >= MAX_QUEUED_SHOTS) {
            return;
        }

        // Add new shot to queue
        shootQueue.offer(currentTime);
        
        // Update bullet speed based on queue size
        double speedMultiplier = 1.0 + (shootQueue.size() * 0.2); // 20% faster per queued shot
        currentBulletSpeed = type.getBulletConfig().speed * speedMultiplier;
        
        BulletConfig config = type.getBulletConfig();

        // Create bullets with current speed
        createBullets(config);
        
        // Play shoot sound
        SoundManager.playShoot();
    }

    // Crée les projectiles selon le type de vaisseau
    private void createBullets(BulletConfig config) {
        // Main bullet
        playerBullets.add(new Bullet(
                x + width / 2 - config.width / 2,
                y,
                config.width,
                config.height,
                -currentBulletSpeed,
                config.color
        ));

        // Side bullets for advanced ships
        if (type == ShipType.FURY || type == ShipType.BLAZE) {
            playerBullets.add(new Bullet(
                    x + width / 4 - config.width / 2,
                    y + height / 4,
                    config.width,
                    config.height,
                    -currentBulletSpeed,
                    config.color
            ));
            playerBullets.add(new Bullet(
                    x + width * 3/4 - config.width / 2,
                    y + height / 4,
                    config.width,
                    config.height,
                    -currentBulletSpeed,
                    config.color
            ));
        }
    }

    @Override
    void draw(GraphicsContext gc) {
        if (shouldDraw()) {
            gc.drawImage(img, x, y, width, getImageHeight());
        }
    }

    @Override
    public void reset() {
        x = GameConfig.BOARD_WIDTH / 2 - GameConfig.TILE_SIZE;
        y = GameConfig.BOARD_HEIGHT - GameConfig.TILE_SIZE * 4;
        lives = 10;
        velocityX = 0;
        shootQueue.clear();
        spaceWasPressed = false;
        setShipType(ShipType.ALPHA); // Ensure ship type is reset to ALPHA
        canShoot = true;
        lastShotTime = 0;
        currentBulletSpeed = 5.0;
        isFlashing = false;
        hitFlashCounter = 0;
        currentFlash = 0;
        activeKeys.clear(); // Add this line to clear any active keys
    }

    public ShipType getType() {
        return type;  // Return the actual type instead of null
    }
}

enum ShipType {
    ALPHA(new BulletConfig(5, 10, 5, Color.web("#18c5ed")), "ships/Alpha.png", 1.0),
    SPECTRA(new BulletConfig(10, 14, 10, Color.web("#ff9b25")), "ships/Spectra.png", 2.0),
    FURY(new BulletConfig(15, 18, 15, Color.web("#ff3e3e")), "ships/Fury.png", 3.0),
    BLAZE(new BulletConfig(20, 22, 20, Color.web("#8dce00")), "ships/Blaze.png", 4.0);

    private final BulletConfig bulletConfig;
    private final String imagePath;
    private final double damageMultiplier;

    ShipType(BulletConfig bulletConfig, String imagePath, double damageMultiplier) {
        this.bulletConfig = bulletConfig;
        this.imagePath = imagePath;
        this.damageMultiplier = damageMultiplier;
    }

    public BulletConfig getBulletConfig() { return bulletConfig; }
    public String getImagePath() { return imagePath; }
    public double getDamageMultiplier() { return damageMultiplier; }

    public int getDamageFor(AlienType alienType) {
        int baseDamage = (ordinal() + 1) * 2;
        double typeMultiplier = switch (alienType) {
            case SPECTER -> 1.5;
            case TOXIN -> switch (this) {
                case ALPHA -> 0.8;
                case SPECTRA -> 1.2;
                case FURY, BLAZE -> 1.4;
            };
            case PHANTOM -> switch (this) {
                case ALPHA, SPECTRA -> 0.6;
                case FURY -> 1.0;
                case BLAZE -> 1.3;
            };
            case CRUSHER -> switch (this) {
                case ALPHA -> 0.4;
                case SPECTRA -> 0.6;
                case FURY -> 0.8;
                case BLAZE -> 1.0;
            };
        };
        return (int)(baseDamage * damageMultiplier * typeMultiplier);
    }

    public int getMaxBullets() {
        return switch (this) {
            case ALPHA -> 1;
            case SPECTRA -> 2;
            case FURY, BLAZE -> 3;
        };
    }
}

class BulletConfig {
    final int width;
    final int height;
    final double speed;
    final Color color;

    BulletConfig(int width, int height, double speed, Color color) {
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.color = color;
    }
}
