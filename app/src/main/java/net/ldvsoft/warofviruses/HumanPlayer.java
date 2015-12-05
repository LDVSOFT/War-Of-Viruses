package net.ldvsoft.warofviruses;

import java.util.Random;

/**
 * Created by Сева on 21.10.2015.
 */
public class HumanPlayer extends Player {
    @Override
    public void makeTurn(Game game) {
        id = new Random().nextInt();
    }
}
