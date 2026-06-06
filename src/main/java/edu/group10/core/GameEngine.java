package edu.group10.core;

import edu.group10.common.model.*;

/**
 * Outward interface of the game core
 *
 * This is the only interface of Core exposed to Infra
 * Infra can use this interface to execute users' actions and get game state
 *
 * Calling process:
 * 1. Infra gets massages from the front end WebSocket, analyses them to PlayerAction
 * 2. Infra calls executeAction() to execute actions
 * 3. Core executes actions then returns GameActionResult
 * 4. Infra changes the result to JSON then sends to teh front end
 *
 * @author Qiutong Dong
 * @version 1.0
 */

public interface GameEngine {
    /**
     * Execute players' actions
     *
     * This is the core method of the game engine. All players' operations (play cards, finish turns, etc.)
     * will come into the game engine through this method.
     *
     * Executing process:
     * 1. Check the validity of actions (whether the player exists, etc.)
     * 2. Allot actions to handler based on their types
     * 3. If the card is an action card, call the Logic model to analyse results of the skill
     * 4. Change the state
     * 5. Check the winning conditions
     * 6. Return results of executing (including the new state and list of events)
     *
     * @param gameId ID of the game rooms (Infra needs to manage the rooms, while Core needs to find the corresponding game via this ID)
     * @param action Objects of players' actions (including action type, card ID, target player, etc.)
     * @throws GameEngineException It will be thrown if the action is invalid or failed to execute
     * @see PlayerAction
     * @see GameActionResult
     */
    GameActionResult executeAction(String gameId, PlayerAction action) throws GameEngineException;

    /**
     * Get the current game state
     *
     * It is used in following state:
     * 1. Disconnection and reconnection of the player: the front end requires the interface of the newest state
     * 2. Witness to the game: the spectator can get the current game state
     * 3. Debugging and testing: get the current state to check
     *
     * Notice: GameState is the snapshot of the public info, it does not include detailed content of other players' cards
     *
     * @param gameId ID of the game room
     * @return Snapshot of the game state (if the game does not exist, return null)
     *
     * @see GameState
     */
    GameState getGameState(String gameId);
}
