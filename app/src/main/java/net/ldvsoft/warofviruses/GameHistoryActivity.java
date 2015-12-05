package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class GameHistoryActivity extends AppCompatActivity {
    private BroadcastReceiver gameHistoryReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gameHistoryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("GameActivityBase", "Broadcast receiver message");
                if (intent.hasExtra(WoVPreferences.LOAD_GAME_HISTORY_KEY)) {
                    Log.d("GameActivityBase", "Game load message received");
                    ArrayList<String> gameHistory = intent.getStringArrayListExtra(WoVPreferences.LOAD_GAME_HISTORY_KEY);
                    if (gameHistory == null) {
                        return;
                    }
                    LinearLayout layout = (LinearLayout) findViewById(R.id.history_layout);
                    for (String game : gameHistory) {
                        String[] data = game.split(";");
                        Button button = new Button(GameHistoryActivity.this);
                        button.setText(data[1]);
                        final int id = Integer.parseInt(data[0]);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(GameHistoryActivity.this, GameActivityReplay.class);
                                intent.putExtra(WoVPreferences.REPLAY_GAME_ID, id);
                                startActivity(intent);
                            }
                        });
                        layout.addView(button);
                    }
                }
            }
        };
        Intent intent = new Intent(this, GameHistoryDBService.class);
        intent.putExtra(WoVPreferences.LOAD_GAME_HISTORY_KEY, "");
        startService(intent);
        registerReceiver(gameHistoryReceiver, new IntentFilter(WoVPreferences.LOAD_GAME_HISTORY_BROADCAST));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gameHistoryReceiver);
    }
}
