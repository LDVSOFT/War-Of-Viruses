package net.ldvsoft.warofviruses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    public void playOnline(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(OPPONENT_TYPE, OPPONENT_NETWORK_PLAYER);
        GoogleCloudMessaging gcm = new GoogleCloudMessaging();
        Bundle data = new Bundle();

        String id = UUID.randomUUID().toString();
        try {
            gcm.send(this.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
