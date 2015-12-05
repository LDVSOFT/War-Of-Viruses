package net.ldvsoft.warofviruses;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Сева on 05.12.2015.
 */
public class GameReplay {
    ArrayList<GameLogic> gameStates;
    private int currentEventNumber;

    GameReplay(ArrayList<GameEvent> gameEventHistory) {
        GameLogic gameLogic = new GameLogic();
        gameLogic.newGame();

        gameStates = new ArrayList<>();
        gameStates.add(new GameLogic(gameLogic));
        for (GameEvent event : gameEventHistory) {
            event.applyEvent(gameLogic);
            gameStates.add(new GameLogic(gameLogic));
        }
        currentEventNumber = 0;
    }

    public void toBeginOfGame() {
        currentEventNumber = 0;
    }

    public void toEndOfGame() {
        currentEventNumber = gameStates.size() - 1;
    }

    public void nextEvent() {
        currentEventNumber = min(currentEventNumber + 1, gameStates.size() - 1);
    }

    public void prevEvent() {
        currentEventNumber = max(0, currentEventNumber - 1);
    }

    public int getEventCount() {
        return gameStates.size();
    }

    public int getCurrentEventNumber() {
        return currentEventNumber + 1;
    }

    public GameLogic getGameLogic() {
        return new GameLogic(gameStates.get(currentEventNumber));
    }
}
