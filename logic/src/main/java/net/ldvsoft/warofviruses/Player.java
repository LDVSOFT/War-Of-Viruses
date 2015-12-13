package net.ldvsoft.warofviruses;

import java.io.Serializable;

/**
 * Created by Сева on 20.10.2015.
 */
public abstract class Player implements Serializable {
    protected GameLogic.PlayerFigure ownFigure;
    protected User user;
    protected Game game;

    public abstract void makeTurn();

    public String getName() {
        return user.getFullNickname();
    }

    public abstract void onGameStateChanged(GameEvent event);

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && user.getId() == ((Player) obj).user.getId();
    }

    public User getUser() {
        return user;
    }
}
