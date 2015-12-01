package net.ldvsoft.warofviruses;

/**
 * Created by Сева on 29.11.2015.
 */
public class GameTurnEvent extends AbstractGameEvent {
    private int turnX, turnY;
    private Player movedPlayer;

    GameTurnEvent(int turnX, int turnY, Player movedPlayer) {
        this.turnX = turnX;
        this.turnY = turnY;
        this.movedPlayer = movedPlayer;
    }

    @Override
    public void applyEvent(Game game) {
        game.doTurn(game.getCurrentPlayer(), turnX, turnY);
    }
}
