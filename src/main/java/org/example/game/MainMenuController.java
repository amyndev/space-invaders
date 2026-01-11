package org.example.game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {
    @FXML private Button btnNewGame;

    @FXML
    protected void startNewGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playing-style.fxml"));
            Parent gameView = loader.load();

            // Get the current stage from any button
            Stage stage = (Stage) btnNewGame.getScene().getWindow();

            // Create and set the new scene
            Scene gameScene = new Scene(gameView);
            stage.setScene(gameScene);
            stage.show();

        } catch (IOException e) {
            showError("Error starting new game", e.getMessage());
        }
    }


    @FXML
    protected void showInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Information");
        alert.setHeaderText("Space Invaders");
        alert.setContentText("""
            Controls:
            - LEFT/RIGHT: Move ship
            - SPACE: Shoot
            - P: Pause game
            - R: Restart game (when game over)
            - ESC: Return to menu
            
            Created by Amine Arfaoui
            Version 1.0
            """);
        alert.showAndWait();
    }

    @FXML
    protected void quitGame() {
        Platform.exit();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
