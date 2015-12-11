package net.ldvsoft.warofviruses;

import java.util.Random;

/**
 * Created by Сева on 21.10.2015.
 */
public class HumanPlayer extends Player {
    public static HumanPlayer deserialize(long id, GameLogic.PlayerFigure ownFigure) {
        HumanPlayer player = new HumanPlayer();
        player.id = id;
        return player;
    }

    @Override
    public void makeTurn(Game game) {
//        id = new Random().nextInt();
        id = 1; // todo: change before adding network player
    }
}
