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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            return user.getId() == ((Player) obj).user.getId();
        }
        return false;
    }

    public User getUser() {
        return user;
    };
}
