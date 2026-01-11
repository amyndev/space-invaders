package org.example.game;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PlayingStyleController {
    @FXML private Button mouseButton;
    @FXML private Button keyboardButton;

    @FXML
    private void initialize() {
        // Add hover effects
        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setWidth(20);
        glow.setHeight(20);

        mouseButton.setOnMouseEntered(e -> mouseButton.setEffect(glow));
        mouseButton.setOnMouseExited(e -> mouseButton.setEffect(null));
        keyboardButton.setOnMouseEntered(e -> keyboardButton.setEffect(glow));
        keyboardButton.setOnMouseExited(e -> keyboardButton.setEffect(null));

        // Set click handlers
        mouseButton.setOnAction(e -> startGame(true));
        keyboardButton.setOnAction(e -> startGame(false));
    }

    private void startGame(boolean useMouse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            Parent gameView = loader.load();
            
            GameController controller = loader.getController();
            controller.setControlStyle(useMouse);
            controller.initializeGame();

            Stage stage = (Stage) mouseButton.getScene().getWindow();
            Scene gameScene = new Scene(gameView);
            
            // Transfer any necessary scene properties
            gameScene.getStylesheets().addAll(mouseButton.getScene().getStylesheets());
            
            stage.setScene(gameScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
