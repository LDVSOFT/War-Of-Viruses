package net.ldvsoft.warofviruses;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Сева on 04.11.2015.
 */
public class GameHistoryDBService extends IntentService {
    private GameHistoryDBOpenHelper gameHistoryDBOpenHelper;
    private final static String TAG = "GameHistoryDBService";

    public GameHistoryDBService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gameHistoryDBOpenHelper = new GameHistoryDBOpenHelper(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gameHistoryDBOpenHelper.close();
    }

    private void saveGame(Intent intent) {
        gameHistoryDBOpenHelper.addGame(intent.getByteArrayExtra(WoVPreferences.GAME_KEY),
                intent.getBooleanExtra(WoVPreferences.GAME_IS_FINISHED_KEY, false));
    }

    private void loadGame() {
        byte[] data = gameHistoryDBOpenHelper.getSerializedActiveGame();

        if (data == null) {
            Log.d("DBService", "FAIL: Null game data loaded");
        } else {
            Log.d("DBService", "OK: game data loaded");
        }

        if (data != null) {
            Intent intent = new Intent(WoVPreferences.LOAD_GAME_BROADCAST);
            intent.putExtra(WoVPreferences.LOAD_GAME_KEY, data);
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "intent recieved!");

        if (intent.hasExtra(WoVPreferences.GAME_KEY)) {
            saveGame(intent);
        } else if (intent.hasExtra(WoVPreferences.LOAD_GAME_KEY)) {
            loadGame();
        } else if (intent.hasExtra(WoVPreferences.LOAD_GAME_HISTORY_KEY)) {
            loadGameHistory();
        } else if (intent.hasExtra(WoVPreferences.LOAD_GAME_BY_ID_KEY)) {
            loadGameById(intent.getIntExtra(WoVPreferences.LOAD_GAME_BY_ID_KEY, 0));
        }
    }

    private void loadGameById(int id) {
        byte[] data = gameHistoryDBOpenHelper.getGameById(id);

        if (data == null) {
            Log.d("DBService", "FAIL: Null game data loaded");
        } else {
            Log.d("DBService", "OK: game data loaded");
        }

        if (data != null) {
            Intent intent = new Intent(WoVPreferences.LOAD_GAME_BY_ID_BROADCAST);
            intent.putExtra(WoVPreferences.LOAD_GAME_BY_ID_KEY, data);
            sendBroadcast(intent);
        }
    }

    private void loadGameHistory() {
        ArrayList<String> history = gameHistoryDBOpenHelper.getGameHistory();
        Intent intent = new Intent(WoVPreferences.LOAD_GAME_HISTORY_BROADCAST);
        intent.putStringArrayListExtra(WoVPreferences.LOAD_GAME_HISTORY_KEY, history);
        sendBroadcast(intent);
    }
}
