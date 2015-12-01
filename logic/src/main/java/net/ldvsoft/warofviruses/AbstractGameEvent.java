package net.ldvsoft.warofviruses;

import java.io.Serializable;

/**
 * Created by Сева on 29.11.2015.
 */
public abstract class AbstractGameEvent implements Serializable {
    public abstract void applyEvent(Game game);
}
