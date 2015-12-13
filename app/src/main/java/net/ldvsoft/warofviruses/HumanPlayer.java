package net.ldvsoft.warofviruses;

import java.util.Random;

/**
 * Created by Сева on 21.10.2015.
 */
public class HumanPlayer extends Player {
    private OnGameStateChangedListener onGameStateChangedListener = null;

    public void setOnGameStateChangedListener(OnGameStateChangedListener onGameStateChangedListener) {
        this.onGameStateChangedListener = onGameStateChangedListener;
    }

    public interface OnGameStateChangedListener {
        void onGameStateChanged(GameLogic gameLogic);
    }

    public static final User USER_ANONYMOUS = new User(
            DBProvider.USER_ANNONYMOUS,
            "uniqueGoogleTokenForAnonymousPlayer",
            0, //DBOpenHelper.playerClasses[0]
            "Anonymous", "1",
            0, 0,
            null);

    public static HumanPlayer deserialize(User user, GameLogic.PlayerFigure ownFigure) {
        return new HumanPlayer(user, ownFigure);
    }

    public HumanPlayer(User user, GameLogic.PlayerFigure ownFigure) {
        this.user = user;
        this.ownFigure = ownFigure;
    }

    public HumanPlayer(User user, GameLogic.PlayerFigure ownFigure, OnGameStateChangedListener onGameStateChangedListener) {
        this.user = user;
        this.ownFigure = ownFigure;
        this.onGameStateChangedListener = onGameStateChangedListener;
    }

    @Override
    public void makeTurn(Game game) {
//        id = new Random().nextInt();
    }

    @Override
    public void onGameStateChanged(Game game, GameEvent event) {
        if (onGameStateChangedListener != null) {
            onGameStateChangedListener.onGameStateChanged(game.getGameLogic());
        }
    }
}
