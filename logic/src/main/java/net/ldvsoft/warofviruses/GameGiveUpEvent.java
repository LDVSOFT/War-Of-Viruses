package net.ldvsoft.warofviruses;

/**
 * Created by Сева on 29.11.2015.
 */
public class GameGiveUpEvent extends AbstractGameEvent {
    private Player movedPlayer;

    GameGiveUpEvent(Player movedPlayer) {
        this.movedPlayer = movedPlayer;
    }

    @Override
    public void applyEvent(Game game) {
        game.giveUp(game.getCurrentPlayer());
    }
}