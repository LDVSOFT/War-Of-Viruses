package net.ldvsoft.warofviruses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.UUID;

public class MenuActivity extends AppCompatActivity {
    private GameLoadedFromServerReceiver gameLoadedFromServerReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

    }

    private class RestoreGameDialog {
        private final Runnable loadGame;

        RestoreGameDialog(Runnable loadGame) {
            this.loadGame = loadGame;
        }

        public void execute() {
            new AlertDialog.Builder(MenuActivity.this)
                    .setMessage("Found saved game. Do you want to restore it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new RestoreGame())
                    .setNegativeButton("No", new NewGame())
                    .show();
        }

        private class RestoreGame implements Dialog.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restoreSavedGame(null);
            }
        }

        private class NewGame implements Dialog.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBOpenHelper.getInstance(MenuActivity.this).deleteActiveGame();
                loadGame.run();
            }
        }
    }

    private class PlayAgainstBot implements Runnable{
        @Override
        public void run() {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_BOT);
            startActivity(intent);
        }
    }

    public void playAgainstBot(View view) {
        if (DBOpenHelper.getInstance(this).hasActiveGame()) {
            new RestoreGameDialog(new PlayAgainstBot()).execute();
            return;
        }
        new PlayAgainstBot().run();
    }

    private class PlayAgainstLocalPlayer implements Runnable{
        @Override
        public void run() {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_LOCAL_PLAYER);
            startActivity(intent);
        }
    }

    public void playAgainstLocalPlayer(View view) {
        if (DBOpenHelper.getInstance(this).hasActiveGame()) {
            new RestoreGameDialog(new PlayAgainstLocalPlayer()).execute();
            return;
        }
        new PlayAgainstLocalPlayer().run();
    }

    public void viewGameHistory(View view) {
        Intent intent = new Intent(this, GameHistoryActivity.class);
        startActivity(intent);
    }

    private class GameLoadedFromServerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GameActivity", "networkLoadGame broadcast recieved!");
            Bundle tmp = intent.getBundleExtra(WoVPreferences.GAME_BUNDLE);
            String data = tmp.getString(WoVProtocol.DATA);
            intent = new Intent(MenuActivity.this, GameActivity.class);
            intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_NETWORK_PLAYER);
            intent.putExtra(WoVPreferences.GAME_JSON_DATA, data);
            unregisterReceiver(gameLoadedFromServerReceiver);
            gameLoadedFromServerReceiver = null;
            startActivity(intent);
        }
    }

    private class PlayOnline implements Runnable {
        @Override
        public void run() {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(MenuActivity.this);
            Bundle data = new Bundle();
            data.putString(WoVProtocol.ACTION, WoVProtocol.ACTION_USER_READY);
            String id = UUID.randomUUID().toString();
            try {
                gcm.send(MenuActivity.this.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            gameLoadedFromServerReceiver = new GameLoadedFromServerReceiver();
            registerReceiver(gameLoadedFromServerReceiver, new IntentFilter(WoVPreferences.GAME_LOADED_FROM_SERVER_BROADCAST));
        }
    }

    public void playOnline(View view) {
        if (DBOpenHelper.getInstance(this).hasActiveGame()) {
            new RestoreGameDialog(new PlayOnline()).execute();
            return;
        }
        new PlayOnline().run();
    }

    @Override
    protected void onStop() {
        if (gameLoadedFromServerReceiver != null) {
            unregisterReceiver(gameLoadedFromServerReceiver);
        }
        super.onStop();
    }

    public void clearDB(View view) {
        DBOpenHelper instance = DBOpenHelper.getInstance(this);
        instance.onUpgrade(instance.getReadableDatabase(), 0, 0);
    }

    public void restoreSavedGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_RESTORED_GAME);
        startActivity(intent);
    }
}
