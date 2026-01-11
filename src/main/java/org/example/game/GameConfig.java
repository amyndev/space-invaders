// GameConfig.java
package org.example.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class GameConfig {
    public static final int BOARD_WIDTH = 1200;  // Reduced to account for padding
    public static final int BOARD_HEIGHT = 700; // Adjusted for UI elements
    public static final int TILE_SIZE = 30;
    public static final double BASE_ALIEN_SHOOT_PROBABILITY = 1.0;
    public static final double ALIEN_SHOOT_PROBABILITY_INCREMENT = 1.0;
}


class Bullet extends GameObject {
    private boolean active = true;
    private final double velocityY;
    private final Color color;

    public Bullet(double x, double y, int width, int height, double velocityY, Color color) {
        super(x, y, width, height, null);
        this.velocityY = velocityY;
        this.color = color;
    }

    @Override
    void update() {
        y += velocityY;
        active = y > 0 && y < GameConfig.BOARD_HEIGHT;
    }

    @Override
    void draw(GraphicsContext gc) {
        if (active) {
            gc.setFill(color);
            gc.fillRect(x, y, width, height);
        }
    }

    @Override
    public void reset() {

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

class Explosion {
    private static final int SPRITE_ROWS = 2;
    private static final int SPRITE_COLS = 5;
    private static final int TOTAL_FRAMES = 7;
    private static final int FRAME_DURATION = 3; // Number of game updates per frame

    private final double x, y;
    private final double size; // Single size value for square explosions
    private final Image spriteSheet;
    private int currentFrame = 0;
    private int frameCounter = 0;
    private boolean isActive = true;

    public Explosion(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        // Use the larger dimension to ensure the explosion is visible
        this.size = Math.max(width, height);
        this.spriteSheet = new Image(getClass().getResourceAsStream("/explosion.png"));
    }

    public void update() {
        if (!isActive) return;

        frameCounter++;
        if (frameCounter >= FRAME_DURATION) {
            frameCounter = 0;
            currentFrame++;
            if (currentFrame >= TOTAL_FRAMES) {
                isActive = false;
            }
        }
    }

    public void draw(GraphicsContext gc) {
        if (!isActive) return;

        int row = currentFrame / SPRITE_COLS;
        int col = currentFrame % SPRITE_COLS;

        double frameSize = spriteSheet.getWidth() / SPRITE_COLS; // Frames are square, so we only need one dimension

        gc.drawImage(spriteSheet,
                col * frameSize,
                row * frameSize,
                frameSize,
                frameSize,
                x - size/2,
                y - size/2,
                size,
                size);
    }

    public boolean isActive() {
        return isActive;
    }
}

class LevelStructure {
    private final AlienType[][] layout;

    public LevelStructure(AlienType[][] layout) {
        this.layout = layout;
    }

    public AlienType[][] getLayout() {
        return layout;
    }

    public int getRows() {
        return layout.length;
    }

    public int getColumns() {
        return layout[0].length;
    }
}
