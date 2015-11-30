package net.ldvsoft.warofviruses;

/**
 * Created by Сева on 29.11.2015.
 */
public class GameSkipTurnEvent extends AbstractGameEvent {
    private Player movedPlayer;
    GameSkipTurnEvent(Player movedPlayer) {
        this.movedPlayer = movedPlayer;
    }

    @Override
    public void applyEvent(Game game) {
        game.skipTurn(movedPlayer);
    }
}
