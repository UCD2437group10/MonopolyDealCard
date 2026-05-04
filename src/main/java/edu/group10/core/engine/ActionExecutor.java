package edu.group10.core.engine;

import edu.group10.common.enums.ActionType;
import edu.group10.common.enums.CardType;
import edu.group10.common.enums.GamePhase;
import edu.group10.common.model.*;
import edu.group10.core.GameEngineException;
import edu.group10.core.handler.*;
import edu.group10.core.manager.CardManager;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;
import edu.group10.core.model.TurnContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Action executor
 * Execute PlayerAction sent from the front end
 * TODO: 等 Logic 模块完成后，需要：
 *  * 1. 注入 ActionHandler
 *  * 2. 在 handlePlayCard() 中补充 ACTION 类型的处理
 */
public class ActionExecutor {
    private final CardManager cardManager;
    private final RuleValidator ruleValidator;
    private final CommandExecutor commandExecutor;
    private final VictoryChecker victoryChecker;

    private final PropertyHandler propertyHandler;
    private final MoneyHandler moneyHandler;
    private final EndTurnHandler endTurnHandler;
    private final DrawCardHandler drawCardHandler;
    private final DiscardHandler discardHandler;

    // TODO: 等 Logic 模块完成后，取消注释
    // private final ActionHandler actionHandler;

    public ActionExecutor(CardManager cardManager,
                          RuleValidator ruleValidator,
                          CommandExecutor commandExecutor,
                          VictoryChecker victoryChecker) {
        this.cardManager = cardManager;
        this.ruleValidator = ruleValidator;
        this.commandExecutor = commandExecutor;
        this.victoryChecker = victoryChecker;

        this.propertyHandler = new PropertyHandler();
        this.moneyHandler = new MoneyHandler();
        this.endTurnHandler = new EndTurnHandler();
        this.drawCardHandler = new DrawCardHandler();
        this.discardHandler = new DiscardHandler();

        // TODO: 等 Logic 模块完成后，取消注释
        // this.actionHandler = new ActionHandler(commandExecutor);
    }

    // TODO: 等 Logic 模块完成后，取消注释
    // public void setSkillResolver(SkillResolver skillResolver) {
    //     actionHandler.setSkillResolver(skillResolver);
    // }

    /**
     * Execute players' action
     */
    public ExecutionResult execute(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        validateBasics(state, action);

        List<GameEvent> events = new ArrayList<>();

        switch (action.getType()) {
            case PLAY_CARD:
                events = handlePlayCard(state, action);
                break;

            case END_TURN:
                events = handleEndTurn(state, action);
                break;

            case DRAW_CARD:
                events = handleDrawCard(state, action);
                break;

            case DISCARD_CARDS:
                events = handleDiscard(state, action);
                break;

            case SELECT_TARGET:
                events = handleSelectTarget(state, action);
                break;

            case CONFIRM:
                events = handleConfirm(state, action);
                break;

            case CANCEL:
                events = handleCancel(state, action);
                break;

            case DECLARE_VICTORY:
                events = handleDeclareVictory(state, action);
                break;

            default:
                throw new GameEngineException("UNKNOWN_ACTION",
                        "Unknown action type: " + action.getType());
        }

        victoryChecker.updateAllPlayersCompletedSets(state);

        String winnerId = victoryChecker.checkAndGetWinner(state);
        if (winnerId != null) {
            state.setPhase(GamePhase.ENDED);
            state.setWinnerId(winnerId);
            events.add(GameEvent.gameOver(winnerId));
        }

        if (state.getPhase() == GamePhase.PLAYING &&
                action.getType() != ActionType.CANCEL &&
                action.getType() != ActionType.CONFIRM &&
                action.getType() != ActionType.SELECT_TARGET) {
            state.getTurnContext().decrementActionsLeft();
        }

        checkHandLimit(state, events);

        return new ExecutionResult(state, events);
    }

    //Basically validate

    private void validateBasics(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        if (state == null) {
            throw new GameEngineException("GAME_NOT_FOUND", "The game unexists");
        }

        if (state.getPhase() == GamePhase.ENDED) {
            throw new GameEngineException("GAME_ENDED", "The game ends");
        }

        Player player = state.getPlayer(action.getPlayerId());
        if (player == null) {
            throw new GameEngineException("PLAYER_NOT_FOUND", "The player unexists");
        }

        if (state.getPhase() == GamePhase.PLAYING) {
            Player currentPlayer = state.getCurrentPlayer();
            if (currentPlayer == null || !currentPlayer.getPlayerId().equals(action.getPlayerId())) {
                throw new GameEngineException("NOT_YOUR_TURN", "It is not your turn");
            }
        }

        if (state.getTurnContext().isTimedOut()) {
            throw new GameEngineException("TURN_TIMEOUT", "The turn is overtime");
        }
    }

    //Action handler

    /**
     * Handle with playing cards
     *
     * TODO: 等 Logic 模块完成后，补充 ACTION 类型的处理
     */
    private List<GameEvent> handlePlayCard(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        Player player = state.getPlayer(action.getPlayerId());
        Card card = cardManager.getCardById(action.getCardId());

        if (card == null) {
            throw new GameEngineException("CARD_NOT_FOUND", "The card unexists");
        }

        if (!player.hasCard(card.getCardId())) {
            throw new GameEngineException("CARD_NOT_IN_HAND", "You do not have this card in your hand cards");
        }

        if (!ruleValidator.canPlayCard(player, card, state)) {
            throw new GameEngineException("CANNOT_PLAY_CARD", "You cannot play this card currently");
        }

        player.removeCardFromHand(card.getCardId());

        List<GameEvent> events;

        switch (card.getCardType()) {
            case PROPERTY:
                events = propertyHandler.handle(state, player, (Property) card, action);
                break;

            case MONEY:
                events = moneyHandler.handle(state, player, card, action);
                break;

            case ACTION:
                // TODO: 等 Logic 模块完成后，取消注释以下代码
                // if (actionHandler == null) {
                //     throw new GameEngineException("ACTION_HANDLER_NOT_READY", "行动卡处理器未就绪");
                // }
                // events = actionHandler.handle(state, player, card, action);

                // 临时：返回空事件并打印日志
                System.out.println("[ActionExecutor] 行动卡暂未实现: " + card.getCardName());
                events = new ArrayList<>();
                break;

            default:
                throw new GameEngineException("UNKNOWN_CARD_TYPE", "Unknown card type: " + card.getCardType());
        }

        if (card.getCardType() != CardType.ACTION) {
            state.getDiscardPile().add(card);
        }

        events.add(0, GameEvent.cardPlayed(player.getPlayerId(), card.getCardName(),
                action.getTargetPlayerId()));

        return events;
    }

    private List<GameEvent> handleEndTurn(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        Player player = state.getPlayer(action.getPlayerId());

        if (player.getHandSize() > 7) {
            throw new GameEngineException("NEED_DISCARD_FIRST",
                    String.format("Hand cards are over 7(currently have %d cards), please discard cards first", player.getHandSize()));
        }

        return endTurnHandler.handle(state, player);
    }

    private List<GameEvent> handleDrawCard(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        Player player = state.getPlayer(action.getPlayerId());
        TurnContext ctx = state.getTurnContext();

        if (!ctx.isCanDrawExtraCard()) {
            throw new GameEngineException("CANNOT_DRAW_NOW", "You cannot draw cards currently");
        }

        ctx.setCanDrawExtraCard(false);
        return drawCardHandler.handle(state, player);
    }

    private List<GameEvent> handleDiscard(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        Player player = state.getPlayer(action.getPlayerId());
        List<String> cardIds = action.getSelectedCardIds();

        if (cardIds == null || cardIds.isEmpty()) {
            throw new GameEngineException("NO_CARDS_SELECTED", "Please choose the card to be discarded first");
        }

        int currentHandSize = player.getHandSize();
        int expectedAfterDiscard = currentHandSize - cardIds.size();

        if (expectedAfterDiscard > 7) {
            throw new GameEngineException("NEED_MORE_DISCARD",
                    String.format("There are still %d cards, please discard cards and let them under 7", expectedAfterDiscard));
        }

        return discardHandler.handle(state, player, cardIds);
    }

    private List<GameEvent> handleSelectTarget(InternalGameState state, PlayerAction action) {
        TurnContext ctx = state.getTurnContext();
        ctx.setPendingTargetPlayerId(action.getTargetPlayerId());
        ctx.setPendingCardId(action.getCardId());

        if (action.getSelectedPropertyId() != null) {
            ctx.setPendingPropertyId(action.getSelectedPropertyId());
        }

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.prompt("Please confirm operation", action.getTargetPlayerId()));
        return events;
    }

    private List<GameEvent> handleConfirm(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        TurnContext ctx = state.getTurnContext();

        if (!ctx.hasPendingAction()) {
            throw new GameEngineException("NOTHING_TO_CONFIRM", "There is no operation that is not be confirmed");
        }

        ctx.clearPending();
        return List.of(GameEvent.confirm("Operation confirmed"));
    }

    private List<GameEvent> handleCancel(InternalGameState state, PlayerAction action) {
        TurnContext ctx = state.getTurnContext();
        ctx.clearPending();
        return List.of(GameEvent.cancel("Operation cancelled"));
    }

    private List<GameEvent> handleDeclareVictory(InternalGameState state, PlayerAction action)
            throws GameEngineException {

        Player player = state.getPlayer(action.getPlayerId());

        if (victoryChecker.isWinner(player, state)) {
            state.setPhase(GamePhase.ENDED);
            state.setWinnerId(player.getPlayerId());
            return List.of(GameEvent.gameOver(player.getPlayerId()));
        } else {
            throw new GameEngineException("VICTORY_CONDITION_NOT_MET",
                    "You do not complete the winning condition (you need to complete 3 sets of property cards)");
        }
    }

    //Helping methods

    private void checkHandLimit(InternalGameState state, List<GameEvent> events) {
        Player currentPlayer = state.getCurrentPlayer();
        if (currentPlayer == null) return;

        int handSize = currentPlayer.getHandSize();
        if (handSize > 7) {
            events.add(GameEvent.warning(
                    String.format("You hand cards are over 7 (currently you have %d cards), please discard cards first", handSize)));
        }
    }

    //Inner classes

    public static class ExecutionResult {
        private final InternalGameState newState;
        private final List<GameEvent> events;

        public ExecutionResult(InternalGameState newState, List<GameEvent> events) {
            this.newState = newState;
            this.events = events;
        }

        public InternalGameState getNewState() { return newState; }
        public List<GameEvent> getEvents() { return events; }
    }
}
