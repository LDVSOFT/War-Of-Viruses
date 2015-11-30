package net.ldvsoft.warofviruses;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

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
        gameHistoryDBOpenHelper.addGame(intent.getByteArrayExtra(GameActivity.GAME_KEY),
                intent.getBooleanExtra(GameActivity.GAME_IS_FINISHED_KEY, false));
    }

    private void loadGame() {
        byte[] data = gameHistoryDBOpenHelper.getSerializedActiveGame();

        if (data == null) {
            Log.d("DBService", "FAIL: Null game data loaded");
        } else {
            Log.d("DBService", "OK: game data loaded");
        }

        if (data != null) {
            Intent intent = new Intent(GameActivity.LOAD_GAME_BROADCAST);
            intent.putExtra(GameActivity.LOAD_GAME_KEY, data);
            sendBroadcast(intent);
//todo: send intent to game
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "intent recieved!");

        if (intent.hasExtra(GameActivity.GAME_KEY)) {
            saveGame(intent);
        } else if (intent.hasExtra(GameActivity.LOAD_GAME_KEY)) {
            loadGame();
        }
    }
}
