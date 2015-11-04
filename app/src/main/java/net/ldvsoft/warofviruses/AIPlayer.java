package net.ldvsoft.warofviruses;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;
import static net.ldvsoft.warofviruses.GameLogic.*;

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
        new BruteforceStrategy(game).execute();
    }

    private class BruteforceStrategy extends AsyncTask<Void, CoordinatePair, Void> {
        private Game game;
        BruteforceStrategy(Game game) {
            this.game = game;
        }

        private double getControlledCellsScore(GameLogic game) {
            int result = 0;
            final double DANGER_FACTOR = 1.5, CONTROL_FACTOR = 1.0;

            switch (game.getCurrentGameState()) {
                case DRAW:
                    return 0;
                case CROSS_WON:
                    return ownFigure == PlayerFigure.CROSS ? +100000 : -100000;
                case ZERO_WON:
                    return ownFigure == PlayerFigure.ZERO  ? +100000 : -100000;
            }
            
            if (game.getCurPlayerFigure() != ownFigure) {
                game.setCurrentPlayerToOpponent();
            }

            for (int sign = 1; sign >= -1; sign -= 2) {

                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        if (game.getCellAt(i, j).isActive()) {
                            result += sign * CONTROL_FACTOR;
                            }
                        if (game.getCellAt(i, j).canMakeTurn() &&
                                game.getCellAt(i, j).getOwner() == game.getOpponent(game.getCurPlayerFigure())) {
                            result += sign * DANGER_FACTOR;
                        }
                    }
                }

                game.setCurrentPlayerToOpponent();
            }

            return result;
        }

        private void runStrategy(GameLogic gameLogic) {
            ArrayList<CoordinatePair> optMoves = bruteforceMoves(gameLogic);
            for (CoordinatePair move : optMoves) {

                publishProgress(move);
                try {
                    sleep(750);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private ArrayList<CoordinatePair> bruteforceMoves(GameLogic gameLogic) {
            ArrayList<CoordinatePair> result = new ArrayList<>();

            if (gameLogic.getCurPlayerFigure() != ownFigure) {
                return result;
            }

            ArrayList<CoordinatePair> moves = gameLogic.getMoves();
            if (moves.size() == 0) {
                return result;
            }

            double optScore = -10000;

            for (CoordinatePair move: moves) {
                GameLogic tmpGameLogic = new GameLogic(gameLogic);
                tmpGameLogic.doTurn(move.x, move.y);
                ArrayList<CoordinatePair> optMoves = bruteforceMoves(tmpGameLogic);
                for (CoordinatePair optMove : optMoves) {
                    tmpGameLogic.doTurn(optMove.x, optMove.y);
                }

                double newScore = getControlledCellsScore(tmpGameLogic);
                if (newScore > optScore) {
                    optScore = newScore;
                    result = optMoves;
                    result.add(0, move);
                }
            }
            return result;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("AIPlayer", "AIPlayer::run");
            Random randomGenerator = new Random();
            GameLogic gameLogic = game.getGameLogic();
            if (!gameLogic.canMove()) {
                publishProgress(new CoordinatePair(-1, -1));
            }
            runStrategy(gameLogic);
            Log.d("AIPlayer", "Turn finished");
            return null;
        }

        @Override
        protected void onProgressUpdate(CoordinatePair... cells) {
            Log.d("AIPlayer", "Update progress: do move to " + cells[0].x + " " + cells[0].y);
            if (cells[0].x < 0) {
                game.skipTurn(AIPlayer.this);
            } else {
                game.doTurn(AIPlayer.this, cells[0].x, cells[0].y);
            }

        }
    }
}
