package net.ldvsoft.warofviruses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Сева on 04.11.2015.
 */
public class GameHistoryDBOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 8;
    private static final String DB_NAME = "gameHistoryDB";
    private static final String GAME_HISTORY_TABLE = "gameHistoryTable";
    private static final String ID = "id";
    private static final String GAME_DATA = "gameData";
    private static final String IS_FINISHED = "isFinished";
    private static final String GAME_DATE = "gameDate";

    private static final String CREATE_TABLE = "CREATE TABLE " + GAME_HISTORY_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            GAME_DATA + " BLOB, " + IS_FINISHED + " INTEGER, " + GAME_DATE + " TEXT);";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + GAME_HISTORY_TABLE + ";";

    private static final String GET_ACTIVE_GAME = "SELECT " + GAME_DATA + " FROM " + GAME_HISTORY_TABLE + " WHERE " + IS_FINISHED + " = 0;";

    private static final String DELETE_ACTIVE_GAME = "DELETE FROM " + GAME_HISTORY_TABLE + " WHERE " + IS_FINISHED + " = 0;";
    private static final String GET_GAME_HISTORY = "SELECT " + ID + ", " + GAME_DATE + " FROM " + GAME_HISTORY_TABLE + " WHERE " +
            IS_FINISHED + " = 1 ORDER BY " + GAME_DATE + " DESC;";
    private static final String GET_GAME_BY_ID = "SELECT " + GAME_DATA + " FROM " + GAME_HISTORY_TABLE + " WHERE " + ID + " = ?;";

    private static GameHistoryDBOpenHelper instance;

    public synchronized static GameHistoryDBOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GameHistoryDBOpenHelper(context);
        }
        return instance;
    }

    private GameHistoryDBOpenHelper(Context context) {// TODO: remove saving at SD card in release
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void addGame(byte[] serializedGame, boolean isFinished) {
        ContentValues cv = new ContentValues();
        cv.put(GAME_DATA, serializedGame);
        cv.put(IS_FINISHED, isFinished);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        cv.put(GAME_DATE, formattedDate);
        getWritableDatabase().insert(GAME_HISTORY_TABLE, null, cv);
    }

    public byte[] getSerializedActiveGame() {
        Cursor cursor = getReadableDatabase().rawQuery(GET_ACTIVE_GAME, null);
        Log.d("DBHelper", "Loading active game: found " + cursor.getCount() + " active games");
        if (!cursor.moveToFirst()) {
            return null;
        }
        byte[] data = cursor.getBlob(0);
        getWritableDatabase().rawQuery(DELETE_ACTIVE_GAME, null);
        deleteActiveGame();
        cursor.close();
        return data;
    }

    public void deleteActiveGame() {
        getWritableDatabase().execSQL(DELETE_ACTIVE_GAME);
    }

    public ArrayList<String> getGameHistory() {
        Cursor cursor = getReadableDatabase().rawQuery(GET_GAME_HISTORY, null);
        Log.d("DBHelper", "Loading game history: found " + cursor.getCount() + " games");
        if (!cursor.moveToFirst()) {
            return null;
        }
        ArrayList<String> history = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            history.add(cursor.getInt(0) + ";" + cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return history;
    }

    public byte[] getGameById(int id) {
        Cursor cursor = getReadableDatabase().rawQuery(GET_GAME_BY_ID, new String[] {Integer.toString(id)});
        Log.d("DBHelper", "Loading game by id: found " + cursor.getCount() + " games");
        if (!cursor.moveToFirst()) {
            return null;
        }
        byte[] data = cursor.getBlob(0);
        cursor.close();
        return data;
    }
}
