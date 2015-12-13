package net.ldvsoft.warofviruses;

import java.io.Serializable;

/**
 * Created by Сева on 20.10.2015.
 */
public abstract class Player implements Serializable {
    protected GameLogic.PlayerFigure ownFigure;
    protected User user;

    public abstract void makeTurn(Game game);

    public String getName() {
        return user.getFullNickname();
    }

    public abstract void onGameStateChanged(Game game, GameEvent event);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && user.getId() == ((Player) obj).user.getId();
    }

    public User getUser() {
        return user;
    }
}
