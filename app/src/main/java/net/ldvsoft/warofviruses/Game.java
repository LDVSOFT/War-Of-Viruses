package net.ldvsoft.warofviruses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import android.util.Log;

/**
 * Created by Сева on 19.10.2015.
 */
public class Game implements Serializable {
    private Player crossPlayer, zeroPlayer;

    private GameLogic gameLogic;

    private transient OnGameStateChangedListener onGameStateChangedListener = null;

    private ArrayList<AbstractGameEvent> gameEventHistory = null;

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

    public interface OnGameStateChangedListener {
        void onGameStateChanged();
    }

    //returns COPY of gameLogic instance to prevent corrupting it
    public GameLogic getGameLogic() {
        return new GameLogic(gameLogic);
    }

    public void setOnGameStateChangedListener(OnGameStateChangedListener onGameStateChangedListener) {
        this.onGameStateChangedListener = onGameStateChangedListener;
    }

    public void startNewGame(Player cross, Player zero) {
        if (gameEventHistory != null) {
            //todo:: save game
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
            gameEventHistory.add(new GameGiveUpEvent(sender));
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
            gameEventHistory.add(new GameSkipTurnEvent(sender));
            onGameStateChangedListener.onGameStateChanged();
        }
        return result;
    }

    public boolean doTurn(Player sender, int x, int y) {
        Log.d("Game", "Do turn at (" + x + "," + y + "), sender=" + sender + " while current player = " + getCurrentPlayer());
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
            gameEventHistory.add(new GameTurnEvent(x, y, sender));
            onGameStateChangedListener.onGameStateChanged();
        }
        return result;
    }
}
