package net.ldvsoft.warofviruses;

/**
 * Created by Сева on 20.10.2015.
 */
public abstract class Player {
    protected String name;
    protected GameLogic.PlayerFigure ownFigure;

    public abstract void makeTurn(Game game);

    public String getName() {
        return name;
    }
}
