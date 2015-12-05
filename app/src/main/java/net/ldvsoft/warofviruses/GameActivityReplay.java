package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Сева on 01.12.2015.
 */
public class GameActivityReplay extends GameActivityBase {
    private BroadcastReceiver loadedGameReceiver;
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
        loadedGameReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("GameActivityBase", "Broadcast receiver message");
                if (intent.hasExtra(WoVPreferences.LOAD_GAME_BY_ID_KEY)) {
                    Log.d("GameActivityBase", "Game load message received");
                    Game game = Game.fromBytes(intent.getByteArrayExtra(WoVPreferences.LOAD_GAME_BY_ID_KEY));
                    gameReplay = new GameReplay(game.getGameLogic().getEventHistory());
                    initButtons();
                    redrawGame(gameReplay.getGameLogic());
                    game.setOnGameStateChangedListener(new Game.OnGameStateChangedListener() {
                        @Override
                        public void onGameStateChanged() {
                            redrawGame(gameReplay.getGameLogic());
                        }
                    });
                }
            }
        };
        registerReceiver(loadedGameReceiver, new IntentFilter(WoVPreferences.LOAD_GAME_BY_ID_BROADCAST));
        Log.d("GameActivityBase", "onStart");
        Intent intent = new Intent(this, GameHistoryDBService.class);
        intent.putExtra(WoVPreferences.LOAD_GAME_BY_ID_KEY, id);
        startService(intent);

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
        unregisterReceiver(loadedGameReceiver);
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
