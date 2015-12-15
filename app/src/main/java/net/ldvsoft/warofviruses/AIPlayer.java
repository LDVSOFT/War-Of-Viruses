package net.ldvsoft.warofviruses;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import static java.lang.Thread.sleep;
import static net.ldvsoft.warofviruses.GameLogic.ADJACENT_DIRECTIONS;
import static net.ldvsoft.warofviruses.GameLogic.BOARD_SIZE;
import static net.ldvsoft.warofviruses.GameLogic.CoordinatePair;
import static net.ldvsoft.warofviruses.GameLogic.GameState;
import static net.ldvsoft.warofviruses.GameLogic.PlayerFigure;
import static net.ldvsoft.warofviruses.GameLogic.isInside;

/**
 * Created by Сева on 20.10.2015.
 */
public class AIPlayer extends Player {
    public static final User AI_USER = new User(
            DBProvider.USER_AI_PLAYER,
            "uniqueGoogleTokenForAiPlayer",
//            1, //DBOpenHelper.playerClasses[1]
            "SkyNet", "1",
            0, 0,
            null);
    private AsyncTask<Void, CoordinatePair, Void> runningStrategy;

    public AIPlayer(GameLogic.PlayerFigure ownFigure) {
        this.ownFigure = ownFigure;
        this.user = AI_USER;
        this.type = 1;
    }

    public static AIPlayer deserialize(User user, GameLogic.PlayerFigure ownFigure, Context context) {
        // There is only one AI user
        return new AIPlayer(ownFigure);
    }

    @Override
    public void makeTurn() {
        Log.d("AIPlayer", "Turn passed to AI player");
        runningStrategy = new BruteforceStrategy(game);
        runningStrategy.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (runningStrategy != null) {
            runningStrategy.cancel(true);
        }
    }

    @Override
    public void onGameStateChanged(GameEvent event, Player whoChanged) {
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
        if (game.getCurrentPlayer().equals(this)) {
            makeTurn();
        }
    }

    private class BruteforceStrategy extends AsyncTask<Void, CoordinatePair, Void> {
        private Game game;
        BruteforceStrategy(Game game) {
            this.game = game;
        }

        //always returns score for ai player.
        private double getEndGameScore(GameLogic game) {
            switch (game.getCurrentGameState()) {
                case CROSS_WON:
                    return ownFigure == PlayerFigure.CROSS ? +100000 : -100000;
                case ZERO_WON:
                    return ownFigure == PlayerFigure.ZERO  ? +100000 : -100000;
                default:
                    return 0;
            }
        }

        //always returns score for ai player.
        private double getConnectivityScore(GameLogic game) {
            final double CONTROL_FACTOR = 1.0, CONNECTIVITY_FACTOR = 0.2;
            final int ADJACENT_SAFE_CNT = 3;

            if (game.getCurrentGameState() != GameState.RUNNING) {
                return getEndGameScore(game);
            }

            double result = 0;

            boolean currentPlayerChanged = false;
            if (game.getCurrentPlayerFigure() != ownFigure) {
                game.setCurrentPlayerToOpponent();
                currentPlayerChanged = true;
            }

            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (game.getCellAt(i, j).isActive()) {
                        result += CONTROL_FACTOR;
                    }
                    if (game.getCellAt(i, j).getOwner() == ownFigure) {
                        int adjacentCnt = 0;
                        for (int [] dir : ADJACENT_DIRECTIONS) {
                            int dx = i + dir[0], dy = j + dir[1];
                            if (isInside(dx) && isInside(dy) && game.getCellAt(dx, dy).getOwner() == ownFigure) {
                                adjacentCnt++;
                            }
                        }
                        if (adjacentCnt < ADJACENT_SAFE_CNT) {
                            result -= CONNECTIVITY_FACTOR;
                        }
                    }

                }
            }

            //we've probably just damaged current game state
            if (currentPlayerChanged) {
                game.setCurrentPlayerToOpponent();
            }

            return result;
        }

        //always returns score for ai player.
        private double getControlledCellsScore(GameLogic game) {
            final double DANGER_FACTOR = 1.25, CONTROL_FACTOR = 1.0;

            if (game.getCurrentGameState() != GameState.RUNNING) {
                return getEndGameScore(game);
            }

            double result = 0;

            boolean currentPlayerChanged = false;
            if (game.getCurrentPlayerFigure() != ownFigure) {
                game.setCurrentPlayerToOpponent();
                currentPlayerChanged = true;
            }

            for (int sign = 1; sign >= -1; sign -= 2) {

                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        if (game.getCellAt(i, j).isActive()) {
                            result += sign * CONTROL_FACTOR;
                            }
                        if (game.getCellAt(i, j).canMakeTurn() &&
                                game.getCellAt(i, j).getOwner() == game.getOpponent(game.getCurrentPlayerFigure())) {
                            result += sign * DANGER_FACTOR;
                        }
                    }
                }

                game.setCurrentPlayerToOpponent();
            }

            //we've probably just damaged current game state
            if (currentPlayerChanged) {
                game.setCurrentPlayerToOpponent();
            }

            return result;
        }

        private void runStrategy(GameLogic gameLogic) {
            ArrayList<CoordinatePair> optMoves = bruteforceMoves(gameLogic);
            if (isCancelled() || optMoves == null) {
                return;
            }

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
            if (isCancelled()) {
                return null;
            }

            ArrayList<CoordinatePair> result = new ArrayList<>();

            if (gameLogic.getCurrentPlayerFigure() != ownFigure) {
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

                double newScore = gameLogic.getCurrentTurn() < 8 ? getConnectivityScore(tmpGameLogic) :
                        getControlledCellsScore(tmpGameLogic);
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
