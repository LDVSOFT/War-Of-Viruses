package net.ldvsoft.warofviruses;

import java.util.Random;

/**
 * Created by Сева on 21.10.2015.
 */
public class HumanPlayer extends Player {
    public static final User USER_ANNONYMOUS = new User(
            DBProvider.USER_ANNONYMOUS,
            "",
            "Annonimous", "1",
            0, 0,
            null);

    public static HumanPlayer deserialize(User user, GameLogic.PlayerFigure ownFigure) {
        return new HumanPlayer(user, ownFigure);
    }

    public HumanPlayer(User user, GameLogic.PlayerFigure ownFigure) {
        this.user = user;
        this.ownFigure = ownFigure;
    }

    @Override
    public void makeTurn(Game game) {
//        id = new Random().nextInt();
    }
}
