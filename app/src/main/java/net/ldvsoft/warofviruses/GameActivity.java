package net.ldvsoft.warofviruses;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static net.ldvsoft.warofviruses.GameLogic.BOARD_SIZE;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_BOT;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_LOCAL_PLAYER;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_NETWORK_PLAYER;
import static net.ldvsoft.warofviruses.MenuActivity.OPPONENT_TYPE;

public class GameActivity extends GameActivityBase {
    private static Gson gson = new Gson();

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

    private class OnExitActivityListener implements DialogInterface.OnClickListener {
        private boolean saveGame;

        public OnExitActivityListener(boolean saveGame) {
            this.saveGame = saveGame;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (!saveGame)
                game = null;
            GameActivity.super.onBackPressed();
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to save current game?")
                .setCancelable(false)
                .setPositiveButton("Yes", new OnExitActivityListener(true))
                .setNegativeButton("No", new OnExitActivityListener(false))
                .show();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                findViewById(R.id.game_bar_replay).setVisibility(View.GONE);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                findViewById(R.id.game_bar_replay_left ).setVisibility(View.GONE);
                findViewById(R.id.game_bar_replay_right).setVisibility(View.GONE);
                break;
        }

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
            case OPPONENT_NETWORK_PLAYER:
                loadGameFromJson(intent.getStringExtra(WoVPreferences.GAME_JSON_DATA));
                break;
            default:
                Log.wtf("GameActivityBase", "Could not start new game: incorrect opponent type");
        }
        initButtons();
        if (game != null) {
            redrawGame(game.getGameLogic());
        }
    }

    @Override
    protected void onPause() {
        Log.d("GameActivityBase", "onPause");
        if (game != null) {
            saveCurrentGame();
            game.onStop();
        }
        unregisterReceiver(gameLoadedFromServerReceiver);

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
                    if (!game.skipTurn(humanPlayer)) {
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
                    game.giveUp(humanPlayer);
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
                    game.doTurn(humanPlayer, x, y);
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
        super.onStop();
    }

    protected void onResume() {
        super.onResume();
        new StoredGameLoader().execute();
        gameLoadedFromServerReceiver = new GameLoadedFromServerReceiver();
        registerReceiver(gameLoadedFromServerReceiver, new IntentFilter(WoVPreferences.GAME_LOADED_FROM_SERVER_BROADCAST));

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

    private class GameLoadedFromServerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GameActivity", "networkLoadGame broadcast recieved!");
            Bundle tmp = intent.getBundleExtra(WoVPreferences.GAME_BUNDLE);
            String data = tmp.getString(WoVProtocol.DATA);
            loadGameFromJson(data);
        }
    }

    private void loadGameFromJson(String data) {
        JsonObject jsonData = (JsonObject) new JsonParser().parse(data);
        User cross = gson.fromJson(jsonData.get(WoVProtocol.CROSS_USER), User.class);
        User zero = gson.fromJson(jsonData.get(WoVProtocol.ZERO_USER), User.class);

        DBOpenHelper.getInstance(GameActivity.this).addUser(cross);
        DBOpenHelper.getInstance(GameActivity.this).addUser(zero);

        GameLogic.PlayerFigure myFigure = gson.fromJson(jsonData.get(WoVProtocol.MY_FIGURE),
                GameLogic.PlayerFigure.class);
        Player playerCross, playerZero;

        switch (myFigure) {
            case CROSS:
                playerZero = new ClientNetworkPlayer(cross, GameLogic.PlayerFigure.ZERO, GameActivity.this);
                playerCross = humanPlayer = new HumanPlayer(zero, GameLogic.PlayerFigure.CROSS);
                break;
            case ZERO:
                playerCross = new ClientNetworkPlayer(cross, GameLogic.PlayerFigure.CROSS, GameActivity.this);
                playerZero = humanPlayer = new HumanPlayer(zero, GameLogic.PlayerFigure.ZERO);
                break;
            default:
                throw new IllegalArgumentException("Illegal myFigure value!");
        }

        List<GameEvent> events = (WoVProtocol.getEventsFromIntArray(gson.fromJson(jsonData.get(WoVProtocol.TURN_ARRAY), int[].class)));

        humanPlayer.setOnGameStateChangedListener(ON_GAME_STATE_CHANGED_LISTENER);
        int crossType = myFigure == GameLogic.PlayerFigure.CROSS ? 0 : 2;
        int zeroType = 2 - crossType; //fixme remove magic constants
        game = Game.deserializeGame(gson.fromJson(jsonData.get(WoVProtocol.GAME_ID), int.class),
                playerCross, crossType, playerZero, zeroType, GameLogic.deserialize(events));
        initButtons();
        redrawGame(game.getGameLogic());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void onGameLoaded(Game game) {
        this.game = game;
        game.updateGameInfo();
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
