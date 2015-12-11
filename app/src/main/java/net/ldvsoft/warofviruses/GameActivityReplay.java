package net.ldvsoft.warofviruses;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Activity that replays saved finished game. Should be created with intent, containing id of game that
 * should be replayed
 */
public class GameActivityReplay extends GameActivityBase {
    private long id;
    GameReplay gameReplay;
    private int turnToStartReplay;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        if (bundle == null) {
            id = intent.getLongExtra(WoVPreferences.REPLAY_GAME_ID, 0);
        } else {
            id = bundle.getLong(WoVPreferences.REPLAY_GAME_ID);
            turnToStartReplay = bundle.getInt(WoVPreferences.REPLAY_GAME_TURN);
        }
        findViewById(R.id.game_bar_play).setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(WoVPreferences.REPLAY_GAME_ID, id);
        if (gameReplay != null) {
            outState.putInt(WoVPreferences.REPLAY_GAME_TURN, gameReplay.getCurrentEventNumber());
        } else {
            outState.putInt(WoVPreferences.REPLAY_GAME_TURN, 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsyncTask<Void, Void, Void>() {
            private Game game;

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (game!= null) {
                    onGameLoaded(game);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                game = DBOpenHelper.getInstance(GameActivityReplay.this).getGameById(id);

                if (game == null) {
                    Log.d("GameActivityReplay", "FAIL: Null game loaded");
                } else {
                    Log.d("GameActivityReplay", "OK: game loaded");
                }

                return null;
            }
        }.execute();
    }

    private void onGameLoaded(Game game) {
        gameReplay = new GameReplay(game.getGameLogic().getEventHistory());
        initButtons();
        for (int i = 0; i < turnToStartReplay - 1; i++) {
            gameReplay.nextEvent();
        }
        redrawGame(gameReplay.getGameLogic());
    }

    @Override
    protected void redrawGame(GameLogic gameLogic) {
        super.redrawGame(gameLogic);
        if (gameReplay != null) {
            ((TextView) findViewById(R.id.game_text_game_position)).setText(String.format("%d/%d",
                    gameReplay.getCurrentEventNumber(), gameReplay.getEventCount()));
        }
    }
    @Override
    protected void onStop() {
        Log.d("GameActivityBase", "onStop");
        super.onStop();
    }

    private void initButtons() {
        findViewById(R.id.game_button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameReplay.toBeginOfGame();
                redrawGame(gameReplay.getGameLogic());
            }
        });
        findViewById(R.id.game_button_last).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameReplay.toEndOfGame();
                redrawGame(gameReplay.getGameLogic());
            }
        });
        findViewById(R.id.game_button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameReplay.nextEvent();
                redrawGame(gameReplay.getGameLogic());
            }
        });
        findViewById(R.id.game_button_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameReplay.prevEvent();
                redrawGame(gameReplay.getGameLogic());
            }
        });

    }

}
