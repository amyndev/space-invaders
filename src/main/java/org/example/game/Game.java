// Game.java
package org.example.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// Classe principale du jeu: gestion de la boucle de jeu et des interactions
public class Game extends Pane {
    // Add this interface inside Game class
    public interface GameUpdateHandler {
        void onUpdate(int score, int level, int lives);
    }
    
    private GameUpdateHandler updateHandler;
    
    // Add this method
    public void setOnGameUpdate(GameUpdateHandler handler) {
        this.updateHandler = handler;
    }
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private GameState gameState = GameState.PLAYING;
    private final boolean useMouseControl;

    // Game objects
    private final Ship ship;
    private final ArrayList<Alien> aliens = new ArrayList<>();
    private final ArrayList<Bullet> playerBullets = new ArrayList<>();
    private final ArrayList<Bullet> alienBullets = new ArrayList<>();
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final GameManager gameManager;
    private final ArrayList<Explosion> explosions = new ArrayList<>();

    // Resources
    private final Image shipImg;
    private final ArrayList<Image> alienImgArray = new ArrayList<>();

    private static final int STAR_COUNT = 100;
    private final ArrayList<Star> stars = new ArrayList<>();

    private void loadAlienImages() {
        alienImgArray.clear();
        for (AlienType type : AlienType.values()) {
            Image img = new Image(getClass().getResourceAsStream("/" + type.getImagePath()));
            alienImgArray.add(img);
        }
    }

    public Game(boolean useMouse) {
        this.useMouseControl = useMouse;
        // Initialize canvas with fixed size
        canvas = new Canvas(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        // Center the canvas in the pane
        canvas.setTranslateX(0);
        canvas.setTranslateY(0);
        
        getChildren().add(canvas);
        
        // Ensure the pane stays the correct size
        setMinSize(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);
        setPrefSize(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);
        setMaxSize(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);

        // Load resources
        shipImg = new Image(getClass().getResourceAsStream("/ships/Alpha.png"));
        loadAlienImages(); // Replace the old alien image loading with this method call

        // Initialize game objects
        ship = new Ship(
                GameConfig.BOARD_WIDTH / 2 - GameConfig.TILE_SIZE,
                GameConfig.BOARD_HEIGHT - GameConfig.TILE_SIZE * 2,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE,
                shipImg,
                activeKeys,
                playerBullets
        );

        ship.setMouseControl(useMouse);
        gameManager = new GameManager(aliens, alienBullets);

        // Initialize stars
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }

        // Start background music when game initializes
        SoundManager.playBackgroundMusic();
        
        setupInput();
        createAliens();
        startGameLoop();
    }

    private void setupInput() {
        // Common controls for both mouse and keyboard
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P) {
                togglePause();
                e.consume(); // Prevent the event from propagating
                return;
            }
            // Only add key to active keys if game is playing
            if (gameState == GameState.PLAYING) {
                activeKeys.add(e.getCode());
                handleGameControls(e.getCode());
            }
        });

        // Make sure the canvas also receives key events
        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P) {
                togglePause();
                e.consume();
            }
        });
        
        setOnKeyReleased(e -> {
            // Always remove released keys
            activeKeys.remove(e.getCode());
        });

        if (useMouseControl) {
            // Add mouse handlers only when game is in PLAYING state
            setOnMouseClicked(e -> {
                if (gameState == GameState.PLAYING) {
                    ship.shoot();
                }
            });
            setOnMouseMoved(e -> {
                if (gameState == GameState.PLAYING) {
                    ship.updateMousePosition(e.getX());
                }
            });
            setOnMouseDragged(e -> {
                if (gameState == GameState.PLAYING) {
                    ship.updateMousePosition(e.getX());
                }
            });
        }

        setFocusTraversable(true);
    }

    private void handleGameControls(KeyCode code) {
        switch (code) {
            case ESCAPE -> quit();
            case R -> {
                if (gameState == GameState.GAME_OVER) {
                    restartGame();
                }
            }
        }
    }

    private void restartGame() {
        try {
            // Clean up current game resources
            SoundManager.cleanup();
            
            // Load new game scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playing-style.fxml"));
            Parent playingStyleView = loader.load();
            Scene playingStyleScene = new Scene(playingStyleView);
            Stage stage = (Stage) this.getScene().getWindow();
            stage.setScene(playingStyleScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestGameFocus() {
        // Request focus when the game starts
        Platform.runLater(() -> {
            requestFocus();
            canvas.requestFocus();
        });
    }

    private void startGameLoop() {
        Platform.runLater(() -> {
            requestFocus();
            canvas.setFocusTraversable(true);
            canvas.requestFocus();
        });
        
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Handle game state
                if (gameState == GameState.PLAYING) {
                    update();
                }
                // Always draw to show pause/game over screens
                draw();
            }
        };
        gameLoop.start();
    }

    private void update() {
        if (gameState == GameState.PAUSED || gameState == GameState.GAME_OVER) {
            return;
        }
        
        // Update stars
        stars.forEach(Star::update);
        
        ship.update();
        updateGameObjects();
        gameManager.alienShoot();
        checkCollisions();
        
        if (gameManager.checkAliensReachedBottom()) {
            gameOver();
            return;
        }

        if (gameManager.checkNextLevel(ship)) {
            createAliens();
        }
        
        // Update explosions only if game is playing
        explosions.removeIf(explosion -> !explosion.isActive());
        explosions.forEach(Explosion::update);
        
        if (!canvas.isFocused()) {
            canvas.requestFocus();
        }
    }

    private void updateGameObjects() {
        gameManager.updateAliens(ship);
        
        // Update all aliens
        aliens.forEach(Alien::update);

        // Update bullets
        playerBullets.removeIf(bullet -> !bullet.isActive());
        alienBullets.removeIf(bullet -> !bullet.isActive());

        playerBullets.forEach(Bullet::update);
        alienBullets.forEach(Bullet::update);
    }

    // Vérifie les collisions entre les objets du jeu
    private void checkCollisions() {
        // Check for direct alien-ship collision
        for (Alien alien : aliens) {
            if (alien.isAlive() && alien.collidesWith(ship)) {
                addExplosion(ship.x + ship.width/2, ship.y + ship.height/2, ship.width * 1.5, ship.height * 1.5);
                SoundManager.playExplosion();
                gameOver();
                return;
            }
        }

        for (Bullet bullet : playerBullets) {
            for (Alien alien : aliens) {
                if (alien.isAlive() && bullet.isActive() && bullet.collidesWith(alien)) {
                    bullet.setActive(false);
                    alien.damage(ship.getType().getDamageFor(alien.getType()));
                    if (!alien.isAlive()) {
                        addExplosion(alien.x + alien.width/2, alien.y + alien.height/2, alien.width, alien.height);
                        SoundManager.playExplosion();
                        gameManager.decrementAlienCount();
                        // Updated scoring formula
                        int baseScore = 100 * alien.getType().ordinal() + 50;
                        int levelBonus = gameManager.getLevel() * 50;
                        int comboBonus = Math.max(1, gameManager.getCurrentCombo());
                        int finalScore = (baseScore + levelBonus) * comboBonus;
                        gameManager.addScore(finalScore);
                    } else {
                        SoundManager.playHit();
                    }
                    break;
                }
            }
        }

        // Check alien bullets hitting player
        for (Bullet bullet : alienBullets) {
            if (bullet.isActive() && bullet.collidesWith(ship)) {
                bullet.setActive(false);
                ship.decrementLives();
                SoundManager.playHit();
                if (ship.getLives() <= 0) {
                    addExplosion(ship.x + ship.width/2, ship.y + ship.height/2, ship.width * 1.5, ship.height * 1.5);
                    SoundManager.playExplosion();
                    gameOver();
                }
                return;
            }
        }
    }

    private void draw() {
        // Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Draw stars
        stars.forEach(star -> star.draw(gc));

        if (gameState == GameState.GAME_OVER) {
            // Only draw game over message in white
            gc.setFill(Color.WHITE);
            drawCenteredText("GAME OVER", "Press R to restart");
            return;
        }

        // Draw game objects only if not game over
        ship.draw(gc);
        aliens.forEach(alien -> alien.draw(gc));
        playerBullets.forEach(bullet -> bullet.draw(gc));
        alienBullets.forEach(bullet -> bullet.draw(gc));
        explosions.forEach(explosion -> explosion.draw(gc));

        if (gameState == GameState.PAUSED) {
            // Semi-transparent overlay for pause
            gc.setFill(new Color(0, 0, 0, 0.5));
            gc.fillRect(0, 0, getWidth(), getHeight());
            gc.setFill(Color.WHITE);
            drawCenteredText("PAUSED", "Press P to resume");
        }

        // Update HUD through controller
        if (updateHandler != null) {
            updateHandler.onUpdate(
                gameManager.getScore(),
                gameManager.getLevel(),
                ship.getLives()
            );
        }
    }

    private void drawCenteredText(String... lines) {
        gc.setFont(Font.font("Arial", 48));
        double y = GameConfig.BOARD_HEIGHT / 2 - (lines.length * 60.0) / 2;

        for (String line : lines) {
            Text text = new Text(line);
            text.setFont(Font.font("Arial", 48));
            double textWidth = text.getLayoutBounds().getWidth();
            gc.fillText(line, (GameConfig.BOARD_WIDTH - textWidth) / 2, y);
            y += 60;
        }
    }

    // Crée une nouvelle vague d'aliens
    private void createAliens() {
        aliens.clear();
        LevelStructure structure = gameManager.getCurrentLevelStructure();
        AlienType[][] layout = structure.getLayout();
        
        int startX = (GameConfig.BOARD_WIDTH - layout[0].length * GameConfig.TILE_SIZE * 3) / 2;
        int currentY = GameConfig.TILE_SIZE;  // Starting Y position
        int count = 0;

        for (int row = 0; row < layout.length; row++) {
            int maxRowHeight = 0;
            // First pass to determine max height in this row
            for (int col = 0; col < layout[row].length; col++) {
                AlienType type = layout[row][col];
                Image alienImage = alienImgArray.get(type.ordinal());
                double imageHeight = GameConfig.TILE_SIZE * (alienImage.getHeight() / alienImage.getWidth());
                maxRowHeight = Math.max(maxRowHeight, (int)imageHeight);
            }

            // Second pass to create aliens
            for (int col = 0; col < layout[row].length; col++) {
                AlienType type = layout[row][col];
                Image alienImage = alienImgArray.get(type.ordinal());
                
                aliens.add(new Alien(
                    startX + col * (GameConfig.TILE_SIZE * 3),
                    currentY,
                    GameConfig.TILE_SIZE * 2,
                    GameConfig.TILE_SIZE,
                    alienImage,
                    gameManager.getAlienVelocityMultiplier(),
                    type,
                    row == layout.length - 1  // isBottomRow flag
                ));
                count++;
            }
            
            // Add padding based on the tallest alien in the row
            double padding = maxRowHeight * 1.5; // Change this multiplier to adjust spacing
            currentY += maxRowHeight + padding;
        }
        gameManager.setAlienCount(count);
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        gameManager.setHighScore(Math.max(gameManager.getHighScore(), gameManager.getScore()));
        SoundManager.stopBackgroundMusic();
        SoundManager.playGameOver();
    }

    public void togglePause() {
        if (gameState == GameState.GAME_OVER) {
            return;
        }
        // Toggle between PLAYING and PAUSED
        gameState = (gameState == GameState.PLAYING) ? GameState.PAUSED : GameState.PLAYING;
        
        // Toggle background music with pause state
        if (gameState == GameState.PAUSED) {
            activeKeys.clear();
            SoundManager.pauseBackgroundMusic();
        } else {
            requestGameFocus();
            SoundManager.playBackgroundMusic();
        }
    }

    public void restart() {
        resetGame();
    }

    private void resetGame() {
        // Reset sound first
        SoundManager.reset();
        
        // Reset game state
        gameState = GameState.PLAYING;
        activeKeys.clear();
        
        // Clear all game objects
        playerBullets.clear();
        alienBullets.clear();
        explosions.clear();
        aliens.clear();
        
        // Reset ship to initial position
        ship.reset();
        
        // Reset game manager
        gameManager.reset();
        
        // Recreate aliens
        createAliens();
        
        // Reset stars
        stars.forEach(Star::reset);
        
        // Restart background music
        SoundManager.playBackgroundMusic();
        
        // Request focus
        requestGameFocus();
    }

    private void addExplosion(double x, double y, double width, double height) {
        // Use a consistent size for explosions based on the game's tile size
        double explosionSize = GameConfig.TILE_SIZE * 2;
        explosions.add(new Explosion(x, y, explosionSize, explosionSize));
    }

    private void returnToMainMenu() {
        try {
            // Clean up resources before switching scenes
            SoundManager.cleanup();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
            Parent menuView = loader.load();
            Scene menuScene = new Scene(menuView);
            Stage stage = (Stage) this.getScene().getWindow();
            stage.setScene(menuScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void quit() {
        // Clean up resources before closing
        SoundManager.cleanup();
        Stage stage = (Stage) this.getScene().getWindow();
        stage.close();
    }
}

enum GameState {
    PLAYING,
    PAUSED,
    GAME_OVER
}