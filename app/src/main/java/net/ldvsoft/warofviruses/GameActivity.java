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

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static net.ldvsoft.warofviruses.GameLogic.BOARD_SIZE;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_BOT;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_LOCAL_PLAYER;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_TYPE;

public class GameActivity extends GameActivityBase {
    private BroadcastReceiver tokenSentReceiver;
    private BroadcastReceiver gameLoadedFromServerReceiver;
    private Game game;

    private final HumanPlayer.OnGameStateChangedListener ON_GAME_STATE_CHANGED_LISTENER =
            new HumanPlayer.OnGameStateChangedListener() {
                @Override
                public void onGameStateChanged(final GameLogic gameLogic) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            redrawGame(gameLogic);
                        }
                    });
                }
            };
    private HumanPlayer humanPlayer = new HumanPlayer(HumanPlayer.USER_ANONYMOUS, GameLogic.PlayerFigure.CROSS,
            ON_GAME_STATE_CHANGED_LISTENER);

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
                break;
            case OPPONENT_LOCAL_PLAYER:
                game.startNewGame(humanPlayer, new HumanPlayer(humanPlayer.getUser(), GameLogic.PlayerFigure.ZERO));
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

    private class OnSkipTurnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (!game.skipTurn(game.getCurrentPlayer())) {
                        return null;
                    }
                    return null;
                }
            }.execute();

        }
    }

    private class OnGiveUpListener implements  View.OnClickListener {

        @Override
        public void onClick(View v) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    game.giveUp(game.getCurrentPlayer());
                    return null;
                }
            }.execute();
        }
    }

    private class OnBoardClickListener implements View.OnClickListener {
        private final int x, y;

        OnBoardClickListener(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public void onClick(View v) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    game.doTurn(game.getCurrentPlayer(), x, y);
                    return null;
                }
            }.execute();
        }
    }

    private void initButtons() {
        Button skipTurnButton = (Button) findViewById(R.id.game_button_passturn);
        skipTurnButton.setOnClickListener(new OnSkipTurnListener());
        Button giveUpButton = (Button) findViewById(R.id.game_button_giveup);
        giveUpButton.setOnClickListener(new OnGiveUpListener());

        for (int i = 0; i != BOARD_SIZE; i++)
            for (int j = 0; j != BOARD_SIZE; j++) {
                boardButtons[i][j].setOnClickListener(new OnBoardClickListener(i, j));
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
                    DBOpenHelper.getInstance(GameActivity.this).addGame(game);
                }
                return null;
            }
        }.execute(game);
    }

    private final class StoredGameLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Game loadedGame = DBOpenHelper.getInstance(GameActivity.this).getActiveGame();

            if (loadedGame == null) {
                Log.d("GameActivity", "FAIL: Null game loaded");
            } else {
                Log.d("GameActivity", "OK: game loaded");
                game = loadedGame;
                if (game.getCrossPlayer() instanceof HumanPlayer) {
                    ((HumanPlayer) game.getCrossPlayer()).setOnGameStateChangedListener(ON_GAME_STATE_CHANGED_LISTENER);
                } else if (game.getZeroPlayer() instanceof HumanPlayer) {
                    ((HumanPlayer) game.getZeroPlayer()).setOnGameStateChangedListener(ON_GAME_STATE_CHANGED_LISTENER);
                } //it's a dirty hack, don't know how to do better
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (game != null) {
                onGameLoaded(game);
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        new StoredGameLoader().execute();
        gameLoadedFromServerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getBundleExtra(WoVProtocol.GAME_BUNDLE);
                //FUCK IT!!! NOT FROM DB, BUT FROM BUNDLE!! AAAAAAAAAAAARRRRRGH
                //DAT PAIN

                User cross;
                User zero;

                //TODO: now i'm going to convert dat bundle to json and then to gson and then I'LL GODLIKE!!!
                //todo:: don't forget to save users to db
                Player playerCross, playerZero;
                //todo: also I should add to message from server field containing my own figure
                if (cross == null) { //FIXME oooh shiit what we will do with null user???
                    playerCross = new ClientNetworkPlayer(cross, GameLogic.PlayerFigure.CROSS, GameActivity.this);
                    playerZero = humanPlayer = new HumanPlayer(zero, GameLogic.PlayerFigure.ZERO);
                } else {
                    playerZero = new ClientNetworkPlayer(cross, GameLogic.PlayerFigure.ZERO, GameActivity.this);
                    playerCross = humanPlayer = new HumanPlayer(zero, GameLogic.PlayerFigure.CROSS);
                }
                ArrayList<GameEvent> events = WoVProtocol.getEventsFromIntArray(data.getIntArray(WoVProtocol.TURN_ARRAY));
                game = Game.deserializeGame(data.getInt(WoVProtocol.GAME_ID), playerCross, playerZero, GameLogic.deserialize(events));
            }
        };
    }

    private void onGameLoaded(Game game) {
        this.game = game;
        setCurrentGameListeners();
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
