package org.example.game;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GameController {
    @FXML
    private BorderPane gamePane;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label levelLabel;
    @FXML
    private HBox livesContainer;
    private Game game;
    private Image heartImage;
    private Image emptyHeartImage;
    private static final int MAX_LIVES = 10;
    private boolean useMouse = false;

    public GameController() {
    }

    @FXML
    public void initialize() {
        this.heartImage = new Image(this.getClass().getResourceAsStream("/heart.png"));
        this.emptyHeartImage = new Image(this.getClass().getResourceAsStream("/empty_heart.png"));
        
        this.gamePane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                this.setupKeyboardHandlers();
            }

        });
    }

    public void initializeGame() {
        this.startNewGame();
        this.setupGameUpdates();
        // Ensure focus is requested after the game is created
        Platform.runLater(() -> {
            if (this.game != null) {
                this.game.requestGameFocus();
            }
        });
    }

    public void setControlStyle(boolean useMouse) {
        this.useMouse = useMouse;
        if (this.game != null) {
            this.game.requestGameFocus();
        }
    }

    private void startNewGame() {
        if (this.game != null) {
            this.game.restart();
        } else {
            this.game = new Game(useMouse);
            this.gamePane.setCenter(this.game);
            this.game.prefWidthProperty().bind(this.gamePane.widthProperty());
            this.game.prefHeightProperty().bind(this.gamePane.heightProperty());
        }
        this.updateHUD();
        this.setupGameUpdates();
        Platform.runLater(() -> this.game.requestGameFocus());
    }

    private void restartGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playing-style.fxml"));
            Parent playingStyleView = loader.load();
            Stage stage = (Stage) gamePane.getScene().getWindow();
            Scene playingStyleScene = new Scene(playingStyleView);
            stage.setScene(playingStyleScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupGameUpdates() {
        this.game.setOnGameUpdate((score, level, lives) -> {
            this.scoreLabel.setText(String.format("%05d", score));
            this.levelLabel.setText(String.format("%02d", level));
            this.updateLives(lives);
        });
    }

    private void setupKeyboardHandlers() {
        this.gamePane.getScene().addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            switch (event.getCode()) {
                case P -> this.game.togglePause();
                case R -> this.restartGame();
                case ESCAPE -> returnToMainMenu();
            }
        });
    }

    private void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
            Parent menuView = loader.load();
            Scene menuScene = new Scene(menuView);
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setScene(menuScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLives(int lives) {
        this.livesContainer.getChildren().clear();
        for (int i = 0; i < MAX_LIVES; i++) {
            ImageView heart = new ImageView(i < lives ? this.heartImage : this.emptyHeartImage);
            heart.setFitWidth(20.0);
            heart.setPreserveRatio(true);
            this.livesContainer.getChildren().add(heart);
        }
    }

    private void updateHUD() {
        this.scoreLabel.setText("00000");
        this.levelLabel.setText("01");
        this.updateLives(3);
    }
}
