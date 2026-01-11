// GameObject.java
package org.example.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class GameObject {
    protected double x;
    protected double y;
    protected int width;
    protected int height;
    protected Image img;

    protected static final int HIT_FLASH_DURATION = 3;
    protected static final int HIT_FLASH_COUNT = 3;
    protected int hitFlashCounter = 0;
    protected int currentFlash = 0;
    protected boolean isFlashing = false;

    protected GameObject(double x, double y, int width, int height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    public boolean collidesWith(GameObject other) {
        return x < other.x + other.width && x + width > other.x &&
                y < other.y + other.height && y + height > other.y;
    }

    protected double getImageHeight() {
        if (img != null) {
            return width * (img.getHeight() / img.getWidth());
        }
        return height;
    }

    protected void updateHitAnimation() {
        if (isFlashing) {
            hitFlashCounter++;
            if (hitFlashCounter >= HIT_FLASH_DURATION) {
                hitFlashCounter = 0;
                currentFlash++;
                if (currentFlash >= HIT_FLASH_COUNT * 2) {
                    isFlashing = false;
                    currentFlash = 0;
                }
            }
        }
    }

    protected boolean shouldDraw() {
        return !isFlashing || currentFlash % 2 == 0;
    }

    protected void startHitAnimation() {
        isFlashing = true;
        hitFlashCounter = 0;
        currentFlash = 0;
    }

    abstract void update();
    abstract void draw(GraphicsContext gc);

    public abstract void reset();
}

