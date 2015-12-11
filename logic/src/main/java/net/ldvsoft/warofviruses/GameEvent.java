package net.ldvsoft.warofviruses;

import java.io.Serializable;

/**
 * Created by Сева on 05.12.2015.
 */
public class GameEvent implements Serializable {
    public enum GameEventType {TURN_EVENT, SKIP_TURN_EVENT, GIVE_UP_EVENT};

    private int turnX, turnY;
    GameEventType type;
    public GameEvent(int turnX, int turnY, GameEventType type) {
        this.turnX = turnX;
        this.turnY = turnY;
        this.type = type;
    }

    public static GameEvent deserialize(int type, int turnX, int turnY) {
        return new GameEvent(turnX, turnY, GameEventType.values()[type]);
    }

    int getTurnX() {
        return turnX;
    }

    int getTurnY() {
        return turnY;
    }

    int getEventTypeAsInt() {
        return type.ordinal();
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
