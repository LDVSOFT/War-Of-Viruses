package net.ldvsoft.warofviruses;

import java.io.Serializable;

/**
 * Created by Сева on 20.10.2015.
 */
public abstract class Player implements Serializable {
    protected String name;
    protected GameLogic.PlayerFigure ownFigure;
    protected long id;

    public abstract void makeTurn(Game game);

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            return id == ((Player) obj).id;
        }
        return false;
    }

    public long getId() {
        return id;
    }
}
