package org.example.game;

// Gestionnaire des effets sonores et de la musique du jeu
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {
    private static final AudioClip shootSound;
    private static final AudioClip explosionSound;
    private static final AudioClip gameOverSound;
    private static final AudioClip levelUpSound;
    private static final AudioClip hitSound;
    private static MediaPlayer backgroundMusic;
    
    static {
        shootSound = new AudioClip(SoundManager.class.getResource("/sounds/shoot.wav").toExternalForm());
        explosionSound = new AudioClip(SoundManager.class.getResource("/sounds/explosion.wav").toExternalForm());
        gameOverSound = new AudioClip(SoundManager.class.getResource("/sounds/gameover.wav").toExternalForm());
        levelUpSound = new AudioClip(SoundManager.class.getResource("/sounds/levelup.wav").toExternalForm());
        hitSound = new AudioClip(SoundManager.class.getResource("/sounds/hit.wav").toExternalForm());
        
        // Initialize background music
        try {
            Media music = new Media(SoundManager.class.getResource("/sounds/music.wav").toExternalForm());
            backgroundMusic = new MediaPlayer(music);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.setVolume(0.3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set default volumes
        shootSound.setVolume(0.3);
        explosionSound.setVolume(0.5);
        gameOverSound.setVolume(0.6);
        levelUpSound.setVolume(0.5);
        hitSound.setVolume(0.4);
    }
    
    public static void playShoot() {
        shootSound.play();
    }
    
    public static void playExplosion() {
        explosionSound.play();
    }
    
    public static void playGameOver() {
        gameOverSound.play();
    }
    
    public static void playLevelUp() {
        levelUpSound.play();
    }
    
    public static void playHit() {
        hitSound.play();
    }

    // Initialise la musique de fond
    private static void initializeBackgroundMusic() {
        if (backgroundMusic == null) {
            try {
                Media music = new Media(SoundManager.class.getResource("/sounds/music.wav").toExternalForm());
                backgroundMusic = new MediaPlayer(music);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(0.3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void playBackgroundMusic() {
        try {
            initializeBackgroundMusic();
            if (backgroundMusic != null) {
                backgroundMusic.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
    
    public static void pauseBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.pause();
        }
    }

    public static void reset() {
        // Stop all currently playing sounds
        stopBackgroundMusic();
        if (shootSound != null) shootSound.stop();
        if (explosionSound != null) explosionSound.stop();
        if (gameOverSound != null) gameOverSound.stop();
        if (levelUpSound != null) levelUpSound.stop();
        if (hitSound != null) hitSound.stop();
    }

    // Nettoie les ressources audio
    public static void cleanup() {
        reset();
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
            backgroundMusic = null;  // Add this line to ensure proper cleanup
        }
    }
}
