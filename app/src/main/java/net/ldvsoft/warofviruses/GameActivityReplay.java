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
    private int id;
    GameReplay gameReplay;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        id = intent.getIntExtra(WoVPreferences.REPLAY_GAME_ID, 0);
        findViewById(R.id.game_bar_play).setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsyncTask<Void, Void, Void>() {
            private byte[] data;

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (data != null) {
                    onGameLoaded(data);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                data = GameHistoryDBOpenHelper.getInstance(GameActivityReplay.this).getGameById(id);

                if (data == null) {
                    Log.d("DBService", "FAIL: Null game data loaded");
                } else {
                    Log.d("DBService", "OK: game data loaded");
                }

                return null;
            }
        }.execute();
    }

    private void onGameLoaded(byte[] data) {
        Game game = Game.fromBytes(data);
        gameReplay = new GameReplay(game.getGameLogic().getEventHistory());
        initButtons();
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
