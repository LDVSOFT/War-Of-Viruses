package net.ldvsoft.warofviruses;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

/**
 * Created by Сева on 20.10.2015.
 */
public class AIPlayer extends Player {
    public AIPlayer(GameLogic.PlayerFigure ownFigure) {
        this.ownFigure = ownFigure;
    }

    @Override
    public void makeTurn(Game game) {
        Log.d("AIPlayer", "Turn passed to AI player");
        new RandomStrategy(game).execute();
    }

    private class RandomStrategy extends AsyncTask<Void, GameLogic.CoordinatePair, Void> {
        private Game game;
        RandomStrategy(Game game) {
            this.game = game;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("AIPlayer", "AIPlayer::run");
            Random randomGenerator = new Random();
            GameLogic gameLogic = game.getGameLogic();
            if (!gameLogic.canMove()) {
                publishProgress(new GameLogic.CoordinatePair(-1, -1));
            }

            while (gameLogic.getCurPlayerFigure() == AIPlayer.this.ownFigure) {
                ArrayList<GameLogic.CoordinatePair> moves = gameLogic.getMoves();
                if (moves.size() == 0) {
                    break;
                }
                int index = randomGenerator.nextInt(moves.size());
                Log.d("AIPlayer", "do turn at " + moves.get(index).x + " " + moves.get(index).y);
                gameLogic.doTurn(moves.get(index).x, moves.get(index).y);
                publishProgress(moves.get(index));
                try {
                    sleep(750);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d("AIPlayer", "Turn finished");
            return null;
        }

        @Override
        protected void onProgressUpdate(GameLogic.CoordinatePair... cells) {
            Log.d("AIPlayer", "Update progress: do move to " + cells[0].x + " " + cells[0].y);
            if (cells[0].x < 0) {
                game.skipTurn(AIPlayer.this);
            } else {
                game.doTurn(AIPlayer.this, cells[0].x, cells[0].y);
            }

        }
    }
}
