package net.ldvsoft.warofviruses;

import java.security.SecureRandom;

/**
 * Created by Сева on 19.10.2015.
 */
public class Game {
    private long id;
    private Player crossPlayer, zeroPlayer;
    private OnGameFinishedListener onGameFinishedListener = null;

    private GameLogic gameLogic;
    private int zeroType;
    private int crossType;

    public int getCrossType() {
        return crossType;
    }

    public int getZeroType() {
        return zeroType;
    }

    public interface OnGameFinishedListener {
        void onGameFinished();
    }
    public static Game deserializeGame(long id, Player crossPlayer, int crossType, Player zeroPlayer, int zeroType, GameLogic gameLogic) {
        Game game = new Game();
        game.id = id;
        game.crossPlayer = crossPlayer;
        game.zeroPlayer = zeroPlayer;
        game.gameLogic = gameLogic;
        game.crossPlayer.setGame(game);
        game.zeroPlayer.setGame(game);
        game.crossType = crossType;
        game.zeroType = zeroType;
        return game;
    }

    public void updateGameInfo() {
        crossPlayer.updateGameInfo(this);
        zeroPlayer.updateGameInfo(this);
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

    /*
    Is called when owner of this game (activity or server) is destroying to properly finish player work and close their resources
     */
    public void onStop() {
        crossPlayer.onStop();
        zeroPlayer.onStop();
    }
    public long getGameId() {
        return id;
    }

    public int getAwaitingEventNumber() {
        return gameLogic.getEventHistory().size();
    }

    //returns COPY of gameLogic instance to prevent corrupting it
    public GameLogic getGameLogic() {
        return new GameLogic(gameLogic);
    }

    public void startNewGame(Player cross, Player zero) {
        id = new SecureRandom().nextLong();
        crossPlayer = cross;
        zeroPlayer = zero;
        crossType = cross.type;
        zeroType = zero.type;
        gameLogic = new GameLogic();
        gameLogic.newGame();
        crossPlayer.setGame(this);
        zeroPlayer.setGame(this);
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
            currentPlayer.makeTurn();
        }
    }

    public void setOnGameFinishedListener(OnGameFinishedListener onGameFinishedListener) {
        this.onGameFinishedListener = onGameFinishedListener;
    }

    public boolean giveUp(Player sender) {
        if (!sender.equals(getCurrentPlayer())) {
            return false;
        }
        boolean result = gameLogic.giveUp();
        if (result) {
            crossPlayer.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1), sender);
            zeroPlayer.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1), sender);
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
            crossPlayer.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1), sender);
            zeroPlayer.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1), sender);
        }
        return result;
    }

    public void update() {
        crossPlayer.setGame(this);
        zeroPlayer.setGame(this);
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
            crossPlayer.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1), sender);
            zeroPlayer.onGameStateChanged(gameLogic.getEventHistory().get(gameLogic.getEventHistory().size() - 1), sender);
        }
        return result;
    }
}
