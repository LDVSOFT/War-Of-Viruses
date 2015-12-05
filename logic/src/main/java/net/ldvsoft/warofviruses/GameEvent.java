package net.ldvsoft.warofviruses;

import java.io.Serializable;

/**
 * Created by Сева on 05.12.2015.
 */
public class GameEvent implements Serializable {
    public enum GameEventType {TURN_EVENT, SKIP_TURN_EVENT, GIVE_UP_EVENT};

    int turnX, turnY;
    GameEventType type;
    GameEvent(int turnX, int turnY, GameEventType type) {
        this.turnX = turnX;
        this.turnY = turnY;
        this.type = type;
    }

    static GameEvent newGiveUpEvent() {
        return new GameEvent(-1, -1, GameEventType.GIVE_UP_EVENT);
    }

    static GameEvent newSkipTurnEvent() {
        return new GameEvent(-1, -1, GameEventType.SKIP_TURN_EVENT);
    }

    static GameEvent newTurnEvent(int turnX, int turnY) {
        return new GameEvent(turnX, turnY, GameEventType.TURN_EVENT);
    }

    public void applyEvent(GameLogic logic) {
        switch(type) {
            case TURN_EVENT:
                logic.doTurn(turnX, turnY);
                break;

            case SKIP_TURN_EVENT:
                logic.skipTurn();
                break;

            case GIVE_UP_EVENT:
                logic.giveUp();
                break;
        }
    }
}
