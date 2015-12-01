package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import static net.ldvsoft.warofviruses.GameLogic.BOARD_SIZE;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_BOT;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_LOCAL_PLAYER;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_TYPE;

/**
 * Created by Сева on 01.12.2015.
 */
public class GameActivity extends GameActivityBase {
    public static final int PLAY_SERVICES_DIALOG = 9001;
    private BroadcastReceiver loadedGameReceiver = null;
    private BroadcastReceiver tokenSentReceiver;
    private HumanPlayer humanPlayer = new HumanPlayer();
    private boolean isEnemyLocalPlayer = false;

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
        redrawGame();
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
                redrawGame();
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
        unregisterReceiver(loadedGameReceiver);
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
                redrawGame();
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
        Intent intent = new Intent(this, GameHistoryDBService.class);
        intent.putExtra(WoVPreferences.GAME_KEY, game.toBytes());
        intent.putExtra(WoVPreferences.GAME_IS_FINISHED_KEY, game.isFinished());
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadedGameReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("GameActivityBase", "Broadcast receiver message");
                if (intent.hasExtra(WoVPreferences.LOAD_GAME_KEY)) {
                    Log.d("GameActivityBase", "Game load message received");
                    game = Game.fromBytes(intent.getByteArrayExtra(WoVPreferences.LOAD_GAME_KEY));
                    setCurrentGameListeners();
                    isEnemyLocalPlayer = true; //at least for now...
                    initButtons();
                    redrawGame();
                }
            }
        };
        registerReceiver(loadedGameReceiver, new IntentFilter(WoVPreferences.LOAD_GAME_BROADCAST));
        Log.d("GameActivityBase", "onStart");
        Intent intent = new Intent(this, GameHistoryDBService.class);
        intent.putExtra(WoVPreferences.LOAD_GAME_KEY, "");
        startService(intent);

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

        if (id == R.id.test) {
            GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
            int result = availability.isGooglePlayServicesAvailable(this);
            if (result != ConnectionResult.SUCCESS) {
                if (availability.isUserResolvableError(result)) {
                    availability.getErrorDialog(this, result, PLAY_SERVICES_DIALOG).show();
                } else {
                    Toast.makeText(this, "No Google Play Services.", Toast.LENGTH_SHORT).show();
                }
            } else {
                startService(new Intent(this, WoVRegistrationIntentService.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
