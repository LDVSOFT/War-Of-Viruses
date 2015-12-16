package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.UUID;

public class MenuActivity extends AppCompatActivity {
    private final static String USER_TOKEN = "3"; //todo REMOVE IT!!!
    public final static String OPPONENT_TYPE = "net.ldvsoft.warofviruses.OPPONENT_TYPE";
    public final static int OPPONENT_BOT = 0;
    public final static int OPPONENT_LOCAL_PLAYER = 1;
    public static final int OPPONENT_NETWORK_PLAYER = 2;
    private GameLoadedFromServerReceiver gameLoadedFromServerReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

    }

    public void playAgainstBot(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(OPPONENT_TYPE, OPPONENT_BOT);
        startActivity(intent);
    }

    public void playAgainstLocalPlayer(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(OPPONENT_TYPE, OPPONENT_LOCAL_PLAYER);
        startActivity(intent);
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
            intent.putExtra(OPPONENT_TYPE, OPPONENT_NETWORK_PLAYER);
            intent.putExtra(WoVPreferences.GAME_JSON_DATA, data);
            unregisterReceiver(gameLoadedFromServerReceiver);
            gameLoadedFromServerReceiver = null;
            startActivity(intent);
        }
    }

    public void playOnline(View view) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        Bundle data = new Bundle();
        data.putString(WoVProtocol.ACTION, WoVProtocol.ACTION_USER_READY);
        String id = UUID.randomUUID().toString();
        try {
            gcm.send(this.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        gameLoadedFromServerReceiver = new GameLoadedFromServerReceiver();
        registerReceiver(gameLoadedFromServerReceiver, new IntentFilter(WoVPreferences.GAME_LOADED_FROM_SERVER_BROADCAST));
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
}
