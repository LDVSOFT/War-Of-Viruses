package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.UUID;

import static net.ldvsoft.warofviruses.GameLogic.BOARD_SIZE;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_BOT;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_LOCAL_PLAYER;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_TYPE;

public class GameActivity extends GameActivityBase {
    private BroadcastReceiver tokenSentReceiver;
    private HumanPlayer humanPlayer = new HumanPlayer();
    private boolean isEnemyLocalPlayer = false;
    private Game game;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        game = new Game();
        tokenSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = prefs.getBoolean(WoVPreferences.GCM_SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Toast.makeText(GameActivity.this, "YEEEEEEEY!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GameActivity.this, "Oh no, Oh no, Oh no-no-no-no(", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Intent intent = getIntent();
        setCurrentGameListeners();
        switch (intent.getIntExtra(OPPONENT_TYPE, -1)) {
            case OPPONENT_BOT:
                game.startNewGame(humanPlayer, new AIPlayer(GameLogic.PlayerFigure.ZERO));
                isEnemyLocalPlayer = false;
                break;
            case OPPONENT_LOCAL_PLAYER:
                game.startNewGame(humanPlayer, new HumanPlayer());
                isEnemyLocalPlayer = true;
                break;
            default:
                Log.wtf("GameActivityBase", "Could not start new game: incorrect opponent type");
        }
        findViewById(R.id.game_bar_replay).setVisibility(View.GONE);
        initButtons();
        redrawGame(game.getGameLogic());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(tokenSentReceiver);
        super.onPause();
    }
    private void initButtons() {
        Button skipTurnButton = (Button) findViewById(R.id.game_button_passturn);
        skipTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnemyLocalPlayer) {
                    if (!game.skipTurn(game.getCurrentPlayer())) {
                        return;
                    }
                } else {
                    if (!game.skipTurn(humanPlayer)) {
                        return;
                    }
                }
                redrawGame(game.getGameLogic());
            }
        });

        Button giveUpButton = (Button) findViewById(R.id.game_button_giveup);
        giveUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnemyLocalPlayer) {
                    game.giveUp(game.getCurrentPlayer());
                } else {
                    game.giveUp(humanPlayer);
                }
            }
        });

        for (int i = 0; i != BOARD_SIZE; i++)
            for (int j = 0; j != BOARD_SIZE; j++) {
                final int x = i;
                final int y = j;
                boardButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isEnemyLocalPlayer) {
                            game.doTurn(game.getCurrentPlayer(), x, y);
                        } else {
                            game.doTurn(humanPlayer, x, y);
                        }
                    }
                });
            }
    }
    @Override
    protected void onStop() {
        Log.d("GameActivityBase", "onStop");
        saveCurrentGame();
        super.onStop();
    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(tokenSentReceiver, new IntentFilter(WoVPreferences.GCM_REGISTRATION_COMPLETE));
    }

    private void setCurrentGameListeners() {
        game.setOnGameStateChangedListener(new Game.OnGameStateChangedListener() {
            @Override
            public void onGameStateChanged() {
                redrawGame(game.getGameLogic());
            }
        });
        game.setOnGameFinishedListener(new Game.OnGameFinishedListener() {
            @Override
            public void onGameFinished() {
                saveCurrentGame();
            }
        });
    }

    private void saveCurrentGame() {
        new AsyncTask<Game, Void, Void> (){
            @Override
            protected Void doInBackground(Game... params) {
                for (Game game : params) { //actually, there is only one game
                    GameHistoryDBOpenHelper.getInstance(GameActivity.this).addGame(game.toBytes(),
                            game.isFinished());

                }
                return null;
            }
        }.execute(game);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                byte[] data = GameHistoryDBOpenHelper.getInstance(GameActivity.this).getSerializedActiveGame();

                if (data == null) {
                    Log.d("DBService", "FAIL: Null game data loaded");
                } else {
                    Log.d("DBService", "OK: game data loaded");
                }

                if (data != null) {
                    onGameLoaded(data);
                }

                return null;
            }
        }.execute();
    }

    private void onGameLoaded(byte[] data) {
        game = Game.fromBytes(data);
        setCurrentGameListeners();
        isEnemyLocalPlayer = true; //at least for now...
        initButtons();
        redrawGame(game.getGameLogic());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.test2) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg;
                    try {
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(GameActivity.this);

                        Bundle data = new Bundle();
                        data.putString(WoVProtocol.ACTION, WoVProtocol.ACTION_PING);
                        String id = UUID.randomUUID().toString();
                        gcm.send(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    if (msg == null)
                        return;
                    Toast.makeText(GameActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }.execute(null, null, null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
