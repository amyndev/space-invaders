# Game Project

A high-performance JavaFX-based space shooter game featuring dynamic difficulty, ship upgrades, and intense arcade action.

![Logo](https://github.com/user-attachments/assets/353509dc-97f5-4a1d-bfff-9d443274488c)

## Features

- **Dynamic Difficulty**: Levels increase in complexity with faster aliens and challenging patterns.
- **Ship Progression**: Upgrade your ship from Alpha to Blaze as you score higher and survive longer.
    - **Alpha**: Balanced starter ship.
    - **Spectra**: Enhanced firepower.
    - **Fury**: Aggressive multi-shot capabilities.
    - **Blaze**: Ultimate destructive power.
- **Advanced Combat**:
    - **Combo System**: Chain kills to multiply your score.
    - **Strategic Enemies**: Four distinct alien types with unique behaviors.
    - **Lives & Health**: Manage your lives carefully to survive the waves.
- **Modern Tech Stack**: Built with Java 23 and JavaFX 23 for optimal performance.

## Resources

### Ships

| Alpha | Spectra | Fury | Blaze |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/82622657-3f34-462c-9463-7e831dea0733" width="100" /> | <img src="https://github.com/user-attachments/assets/df30242b-e740-4c82-85c1-f9d8b8cd29bc" width="100" /> | <img src="https://github.com/user-attachments/assets/b96a921a-83ef-4a32-8640-3aa66f68306c" width="100" /> | <img src="https://github.com/user-attachments/assets/24f81926-c698-4dc4-a057-19b01dbcb62d" width="100" /> |

### Aliens

| Crusher | Phantom | Specter | Toxin |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/9feace45-3bf9-43c2-af39-f476521b2ee9" width="100" /> | <img src="https://github.com/user-attachments/assets/a150d667-15fb-45c8-bedb-2fc4c82a75e3" width="100" /> | <img src="https://github.com/user-attachments/assets/1106f347-3bfa-414e-9adc-264dc7af43a8" width="100" /> | <img src="https://github.com/user-attachments/assets/d417ee58-a179-4d8a-8955-66c05f749b88" width="100" /> |

## Controls

The game supports both keyboard and mouse controls (selectable in the menu).

### Keyboard Controls
- **Movement**: `←` (Left Arrow) and `→` (Right Arrow)
- **Fire**: `Space`
- **Pause**: `P`
- **Restart**: `R` (When Game Over)
- **Main Menu**: `ESC`

## Prerequisites

- **Java Development Kit (JDK)**: Version 23 or later.
- **Maven**: Version 3.8 or later.

## How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/amyndev/space-invaders/
   ```

2. **Run the application using Maven:**
   ```bash
   mvn clean javafx:run
   ```

## Project Structure

- `src/main/java`: Source code organized by functionality.
- `src/main/resources`: Game assets (images, sounds, FXML views).
- `pom.xml`: Maven project configuration.
