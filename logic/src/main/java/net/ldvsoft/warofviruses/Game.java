package net.ldvsoft.warofviruses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by Сева on 19.10.2015.
 */
public class Game {
    private long id;
    private Player crossPlayer, zeroPlayer;

    private GameLogic gameLogic;

    private OnGameStateChangedListener onGameStateChangedListener = null;
    private OnGameFinishedListener onGameFinishedListener = null;

    public static Game deserializeGame(long id, Player crossPlayer, Player zeroPlayer, GameLogic gameLogic) {
        Game game = new Game();
        game.id = id;
        game.crossPlayer = crossPlayer;
        game.zeroPlayer = zeroPlayer;
        game.gameLogic = gameLogic;
        return game;
    }

    public boolean isFinished() {
        return gameLogic.isFinished();
    }

    public Player getZeroPlayer() {
        return zeroPlayer;
    }

    public Player getCrossPlayer() {
        return crossPlayer;
    }

    public long getGameId() {
        return id;
    }

    public interface OnGameStateChangedListener {
        void onGameStateChanged(GameEvent event);
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
        id = new SecureRandom().nextLong();
        crossPlayer = cross;
        zeroPlayer = zero;
        gameLogic = new GameLogic();
        gameLogic.newGame();
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
        if (!sender.equals(getCurrentPlayer())) {
            return false;
        }
        boolean result = gameLogic.giveUp();
        if (result) {
            onGameStateChangedListener.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1));
        }
        return result;
    }

    public boolean skipTurn(Player sender) {
        if (!sender.equals(getCurrentPlayer())) {
            return false;
        }

        GameLogic.PlayerFigure oldPlayer = gameLogic.getCurrentPlayerFigure();
        boolean result = gameLogic.skipTurn();
        if (result) {
            GameLogic.PlayerFigure currentPlayer = gameLogic.getCurrentPlayerFigure();
            if (!oldPlayer.equals(currentPlayer)) {
                notifyPlayer();
            }
            onGameStateChangedListener.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1));
        }
        return result;
    }

    public boolean doTurn(Player sender, int x, int y) {
        if (!sender.equals(getCurrentPlayer())) {
            return false;
        }

        GameLogic.PlayerFigure oldPlayer = gameLogic.getCurrentPlayerFigure();
        boolean result = gameLogic.doTurn(x, y);
        if (result) {
            GameLogic.PlayerFigure currentPlayer = gameLogic.getCurrentPlayerFigure();
            if (!oldPlayer.equals(currentPlayer)) {
                notifyPlayer();
            }
            onGameStateChangedListener.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1));
        }
        return result;
    }
}
