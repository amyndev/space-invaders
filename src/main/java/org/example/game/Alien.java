// Alien.java
package org.example.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Alien extends GameObject {
    private boolean alive = true;
    private final double velocityMultiplier;
    private final AlienType type;
    private int health;
    private final boolean isBottomRow;

    // Add this constructor overload
    public Alien(double x, double y, int width, int height, Image img, double velocityMultiplier) {
        this(x, y, width, height, img, velocityMultiplier, AlienType.SPECTER, false);
    }

    public Alien(double x, double y, int width, int height, Image img, double velocityMultiplier, AlienType type) {
        this(x, y, width, height, img, velocityMultiplier, type, false);
    }

    public Alien(double x, double y, int width, int height, Image img, double velocityMultiplier, AlienType type, boolean isBottomRow) {
        super(x, y, width, height, img);
        this.velocityMultiplier = velocityMultiplier;
        this.type = type;
        this.health = type.getDurability();
        this.isBottomRow = isBottomRow;
    }

    @Override
    void update() {
        if (alive) {
            x += GameManager.getAlienVelocityX() * velocityMultiplier;
            updateHitAnimation();  // Add hit animation update
        }
    }

    @Override
    void draw(GraphicsContext gc) {
        if (alive && shouldDraw()) {
            gc.drawImage(img, x, y, width, getImageHeight());
        }
    }

    @Override
    public void reset() {

    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void drop() {
        y += height;
    }

    public void damage(int amount) {
        health -= amount;
        if (health <= 0) {
            setAlive(false);
        } else {
            startHitAnimation();
        }
    }

    public AlienType getType() { return type; }

    public boolean canShoot() {
        return isBottomRow && alive;
    }
}


enum AlienType {
    SPECTER("aliens/Specter.png", Color.web("#FFD112"), 1),
    TOXIN("aliens/Toxin.png", Color.web("#e01010"), 2),
    PHANTOM("aliens/Phantom.png", Color.WHITE, 4),
    CRUSHER("aliens/Crusher.png", Color.web("#BF3BE0"), 8);

    private final String imagePath;
    private final Color fireColor;
    private final int durability;

    AlienType(String imagePath, Color fireColor, int durability) {
        this.imagePath = imagePath;
        this.fireColor = fireColor;
        this.durability = durability;
    }

    public String getImagePath() { return imagePath; }
    public Color getFireColor() { return fireColor; }
    public int getDurability() { return durability; }
}
