package net.ldvsoft.warofviruses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Сева on 19.10.2015.
 */
public class Game implements Serializable {
    private Player crossPlayer, zeroPlayer;

    private GameLogic gameLogic;

    private boolean isReplaying = false;
    private transient OnGameStateChangedListener onGameStateChangedListener = null;
    private transient OnGameFinishedListener onGameFinishedListener = null;
    private ArrayList<AbstractGameEvent> gameEventHistory = null;
    private int currentEventNumber;

    public byte[] toBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace(); // at least for now
            return null;
        }
    }

    public boolean isFinished() {
        return gameLogic.isFinished();
    }

    public static Game fromBytes(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            try (ObjectInput in = new ObjectInputStream(bis)) {
                return (Game) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setReplayMode() {
        isReplaying = true;
        toBeginOfGame();
    }

    public void toBeginOfGame() {
        gameLogic.newGame();
        currentEventNumber = 0;
    }

    public void toEndOfGame() {
        for (int i = currentEventNumber; i < gameEventHistory.size(); i++) {
            gameEventHistory.get(i).applyEvent(this);
        }
        currentEventNumber = gameEventHistory.size();
    }

    public void nextEvent() {
        if (currentEventNumber < gameEventHistory.size()) {
            gameEventHistory.get(currentEventNumber++).applyEvent(this);
        }
    }

    public void prevEvent() {
        int lastStep = currentEventNumber;
        toBeginOfGame();
        for (int i = 0; i < lastStep; i++) {
            gameEventHistory.get(i).applyEvent(this);
        }
        currentEventNumber = lastStep == 0 ? 0 : lastStep - 1;
    }

    public int getEventCount() {
        return gameEventHistory.size();
    }

    public int getCurrentEventNumber() {
        return currentEventNumber;
    }


    public interface OnGameStateChangedListener {
        void onGameStateChanged();
    }

    public interface OnGameFinishedListener {
        void onGameFinished();
    }

    //returns COPY of gameLogic instance to prevent corrupting it
    public GameLogic getGameLogic() {
        return new GameLogic(gameLogic);
    }

    public void setOnGameStateChangedListener(OnGameStateChangedListener onGameStateChangedListener) {
        this.onGameStateChangedListener = onGameStateChangedListener;
    }

    public void setOnGameFinishedListener(OnGameFinishedListener onGameFinishedListener) {
        this.onGameFinishedListener = onGameFinishedListener;
    }

    public void startNewGame(Player cross, Player zero) {
        if (gameEventHistory != null) {
            if (onGameFinishedListener != null) {
                onGameFinishedListener.onGameFinished();
            }
        }
        crossPlayer = cross;
        zeroPlayer = zero;
        gameLogic = new GameLogic();
        gameLogic.newGame();
        gameEventHistory = new ArrayList<>();
    }

    public Player getCurrentPlayer() {
        switch (gameLogic.getCurrentPlayerFigure()) {
            case CROSS:
                return crossPlayer;
            case ZERO:
                return zeroPlayer;
            default:
                return null;
        }
    }

    private void notifyPlayer() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayer.makeTurn(this);
        }
    }

    public boolean giveUp(Player sender) {
        if (sender != getCurrentPlayer()) {
            return false;
        }
        boolean result = gameLogic.giveUp();
        if (result) {
            if (!isReplaying) {
                gameEventHistory.add(new GameGiveUpEvent(sender));
            }
            onGameStateChangedListener.onGameStateChanged();
        }
        return result;
    }

    public boolean skipTurn(Player sender) {
        if (sender != getCurrentPlayer()) {
            return false;
        }

        GameLogic.PlayerFigure oldPlayer = gameLogic.getCurrentPlayerFigure();
        boolean result = gameLogic.skipTurn();
        if (result) {
            GameLogic.PlayerFigure currentPlayer = gameLogic.getCurrentPlayerFigure();
            if (oldPlayer != currentPlayer) {
                notifyPlayer();
            }
            if (!isReplaying) {
                gameEventHistory.add(new GameSkipTurnEvent(sender));
            }
            onGameStateChangedListener.onGameStateChanged();
        }
        return result;
    }

    public boolean doTurn(Player sender, int x, int y) {
        if (sender != getCurrentPlayer()) {
            return false;
        }

        GameLogic.PlayerFigure oldPlayer = gameLogic.getCurrentPlayerFigure();
        boolean result = gameLogic.doTurn(x, y);
        if (result) {
            GameLogic.PlayerFigure currentPlayer = gameLogic.getCurrentPlayerFigure();
            if (oldPlayer != currentPlayer) {
                notifyPlayer();
            }
            if (!isReplaying) {
                gameEventHistory.add(new GameTurnEvent(x, y, sender));
            }
            onGameStateChangedListener.onGameStateChanged();
        }
        return result;
    }

}
