# MonopolyDealCard

## main introduction
monopoly deal cards game


## Features

- Single-player mode with bot players
- Multiplayer lobby flow: join, ready, start, reset
- Core card actions:
    - play money cards
    - play property cards
    - change multi-property color
    - charge rent
    - play action cards
    - end turn
- Special rule handling:
    - Just Say No response
    - property stealing / swapping
    - payment by bank and property
    - winner detection
- JavaFX GUI with hand area, board area, dialogs, logs, and animations

## Project Structure

- `src/main/java/edu/group10/monopolydeal/frontend`: JavaFX UI, controllers, client
- `src/main/java/edu/group10/monopolydeal/backend`: game logic, server, protocol, services
- `src/main/java/edu/group10/monopolydeal/launcher/GameLauncher.java`: application entry
- `src/main/resources`: FXML, CSS, card images

## Requirements

- JDK 21
- Gradle wrapper included in the project

Check Java version:

```bash
java -version
```

The output should be JDK 21.

## How to Start

This project is already configured with Gradle. The main class is:

`edu.group10.monopolydeal.launcher.GameLauncher`

### macOS / Linux

```bash
./gradlew run
```

### Windows

```bash
gradlew.bat run
```

What this launcher does:

- starts the local game server
- creates the shared game client
- launches the JavaFX application window

## How to Play

### Single-player

1. Start the program.
2. Choose `Single Player` in the main menu.
3. Enter the game.
4. The system will create bot players and start local gameplay flow.

### Multiplayer

1. Start the program.
2. Choose `Multiplayer`.
3. Fill in host, port, player id, and player name.
4. If you want to host locally, choose the local hosting option in the menu.
5. Enter the game and join the lobby.
6. All human players click `Ready`.
7. The host clicks `Start`.

## Basic Operations

Inside the game window, the main operations are:

- select a card in hand
- play it as money, property, rent, or action
- change the color of a multi-property card when allowed
- select target player or extra parameters for some actions
- view discard pile or property details
- end the turn after finishing actions


## Notes

- The project is built for JDK 21 and JavaFX 21.
- The default launcher is intended for local desktop use.
- If you change code structure or features, remember to keep the report and docs in sync.
