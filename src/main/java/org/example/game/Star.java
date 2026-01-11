package org.example.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Star {
    private double x;
    private double y;
    private double speed;
    private double size;
    private double opacity;

    public Star() {
        reset();
        // Initialize y at random position
        this.y = Math.random() * GameConfig.BOARD_HEIGHT;
    }

    public void reset() {
        this.x = Math.random() * GameConfig.BOARD_WIDTH;
        this.y = 0;
        this.speed = 1 + Math.random() * 3;
        this.size = 1 + Math.random() * 2;
        this.opacity = 0.5 + Math.random() * 0.5;
    }

    public void update() {
        y += speed;
        if (y > GameConfig.BOARD_HEIGHT) {
            reset();
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.rgb(255, 255, 255, opacity));
        gc.fillOval(x, y, size, size);
    }
}
