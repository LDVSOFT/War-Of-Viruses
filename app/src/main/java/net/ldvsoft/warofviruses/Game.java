package net.ldvsoft.warofviruses;

import java.util.ArrayList;

/**
 * Created by Сева on 19.10.2015.
 */
public class Game {
    private Player crossPlayer, zeroPlayer;

    private GameLogic gameLogic;

    private OnGameStateChangedListener onGameStateChangedListener = null;

    public interface OnGameStateChangedListener {
        public void onGameStateChanged();
    }

    //returns COPY of gameLogic instance to prevent corrupting it
    public GameLogic getGameLogic() {
        return new GameLogic(gameLogic);
    }

    public void setOnGameStateChangedListener(OnGameStateChangedListener onGameStateChangedListener) {
        this.onGameStateChangedListener = onGameStateChangedListener;
    }

    public void startNewGame(Player cross, Player zero) {
        crossPlayer = cross;
        zeroPlayer = zero;
        gameLogic = new GameLogic();
        gameLogic.newGame();
    }

    public Player getCurrentPlayer() {
        switch (gameLogic.getCurPlayerFigure()) {
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
        onGameStateChangedListener.onGameStateChanged();
        return result;
    }

    public boolean skipTurn(Player sender) {
        if (sender != getCurrentPlayer()) {
            return false;
        }

        GameLogic.PlayerFigure oldPlayer = gameLogic.getCurPlayerFigure();
        boolean result = gameLogic.skipTurn();
        GameLogic.PlayerFigure currentPlayer = gameLogic.getCurPlayerFigure();
        if (oldPlayer != currentPlayer) {
            notifyPlayer();
        }
        onGameStateChangedListener.onGameStateChanged();
        return result;
    }

    public boolean doTurn(Player sender, int x, int y) {
        if (sender != getCurrentPlayer()) {
            return false;
        }

        GameLogic.PlayerFigure oldPlayer = gameLogic.getCurPlayerFigure();
        boolean result = gameLogic.doTurn(x, y);
        GameLogic.PlayerFigure currentPlayer = gameLogic.getCurPlayerFigure();
        if (oldPlayer != currentPlayer) {
            notifyPlayer();
        }
        onGameStateChangedListener.onGameStateChanged();
        return result;
    }
}
