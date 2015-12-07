package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Activity which displays all the played finished games that stored locally.
 * User can select some game to replay it
 */
public class GameHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);
    }

    private class GameHistoryLoader extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> gameHistory = null;
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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

        @Override
        protected Void doInBackground(Void... params) {
            gameHistory = GameHistoryDBOpenHelper.getInstance(GameHistoryActivity.this).getGameHistory();

            return null;
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        new GameHistoryLoader().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
