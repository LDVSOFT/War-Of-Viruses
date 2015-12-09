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
    private static final String GAME_TABLE = "Game";
    private static final String ID = "id";
    private static final String GAME_DATA = "gameData";
    private static final String GAME_STATUS = "status";
    private static final String GAME_DATE = "gameDate";
    private static final String PLAYER_ZERO = "playerZero";
    private static final String PLAYER_CROSSES = "playerCrosses";

    private static final String TURN_TABLE = "Turn";
    private static final String GAME = "game";
    private static final String TURN_NUMBER = "turnNo";
    private static final String TURN_TYPE = "type";
    private static final String TURN_X = "x";
    private static final String TURN_Y = "y";

    private static final String CREATE_GAME_TABLE = "CREATE TABLE " + GAME_TABLE + "(" + ID + " INT PRIMARY KEY AUTOINCREMENT, " +
            PLAYER_CROSSES + " INT UNSIGNED NOT NULL, " + PLAYER_ZERO + " INT  UNSIGNED NOT NULL, " + GAME_STATUS +
            " INT NOT NULL, " + GAME_DATE + " TEXT);"; //todo: foreign key for player ids

    private static final String CREATE_TURN_TABLE = "CREATE TABLE " + TURN_TABLE + "(" + GAME + " INT UNSIGNED NOT NULL, " +
            TURN_NUMBER + " INT UNSIGNED NOT NULL, " + TURN_TYPE + " INT NOT NULL, " + TURN_X + " INT NULL, " + TURN_Y + " INT NULL, " +
            "PRIMARY KEY(" + GAME + ", " + TURN_NUMBER + "), FOREIGN KEY (" + GAME + ") REFERENCES " + GAME + "(" + ID + ")" + ");";

    private static final String GET_ACTIVE_GAME = "SELECT " + TURN_TYPE + ", " + TURN_X + ", " + TURN_Y + ", " +
            " FROM " + TURN_TABLE + " WHERE " + GAME_STATUS + " = 0 ORDER BY " + TURN_NUMBER + " ASC;";

    private static final String DELETE_ACTIVE_GAME_TURNS = "DELETE t FROM " + TURN_TABLE + "t INNER JOIN " + GAME_TABLE +
            " ON (" + GAME_TABLE + "." + ID + " == " + TURN_TABLE + "." + GAME + " AND NOT " + GAME + "." + GAME_STATUS + ";";

    private static final String DELETE_ACTIVE_GAME = "DELETE FROM " + GAME_TABLE + " WHERE " + GAME_STATUS +
            " =" + GameStatus.RUNNING.ordinal() + ";";

    private static final String GET_GAME_HISTORY = "SELECT " + ID + ", " + GAME_DATE + " FROM " + GAME_TABLE + " WHERE " +
            GAME_STATUS + " = 1 ORDER BY " + GAME_DATE + " DESC;";

    private static final String GET_GAME_BY_ID = "SELECT " + GAME_DATA + " FROM " + GAME_TABLE + " WHERE " + ID + " = ?;";

    private static final String ADD_GAME = "INSERT INTO " + GAME_TABLE + "(" + ID + ", " + PLAYER_CROSSES + ", " + PLAYER_ZERO +
            ", " + GAME_STATUS + ", " + GAME_DATE + ") VALUES (?, ?, ?, ?, ?);";

    private static final String ADD_GAME_TURNS = "INSERT INTO " + TURN_TABLE + "(" + GAME + ", " + TURN_NUMBER + ", " + TURN_TYPE +
            ", " + TURN_X + ", " + TURN_Y + ") VALUES (?, ?, ?, ?, ?);";

    private static GameHistoryDBOpenHelper instance;

    private static final String DROP_GAME_TABLE = "DROP TABLE IF EXISTS " + GAME_TABLE + ";";
    private static final String DROP_TURN_TABLE = "DROP TABLE IF EXISTS " + TURN_TABLE + ";";

    private enum GameStatus {RUNNING, FINISHED, DELETED}; //probably not there, in some other class

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
        db.execSQL(CREATE_GAME_TABLE);
        db.execSQL(CREATE_TURN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_GAME_TABLE);
        db.execSQL(DROP_TURN_TABLE);
        onCreate(db);
    }

    public void addGame(Game game) {
        ContentValues cv = new ContentValues();
        cv.put(GAME_DATA, serializedGame);
        cv.put(GAME_STATUS, isFinished);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        cv.put(GAME_DATE, formattedDate);
        getWritableDatabase().insert(GAME_TABLE, null, cv);
    }

    public Game getSerializedActiveGame() {
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

    public Game getGameById(int id) {
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
