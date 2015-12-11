package net.ldvsoft.warofviruses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Сева on 04.11.2015.
 */
public class DBOpenHelper extends SQLiteOpenHelper implements DBProvider {
    private static final int VERSION = 10;
    private static final String DB_NAME = "gameHistoryDB";

    private static final String CREATE_GAME_TABLE = "CREATE TABLE " + GAME_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PLAYER_CROSSES + " INTEGER UNSIGNED NOT NULL, " + PLAYER_ZERO + " INTEGER  UNSIGNED NOT NULL, " + GAME_STATUS +
            " INT NOT NULL, " + GAME_DATE + " TEXT NOT NULL, FOREIGN KEY (" + PLAYER_CROSSES + ", " + PLAYER_ZERO +
            ") REFERENCES " + USER_TABLE + " (" + ID + ", " + ID + ") ON DELETE CASCADE ON UPDATE CASCADE);";

    private static final String CREATE_TURN_TABLE = "CREATE TABLE " + TURN_TABLE + "(" + GAME_ID + " INTEGER UNSIGNED NOT NULL, " +
            TURN_NUMBER + " INT UNSIGNED NOT NULL, " + TURN_TYPE + " INT NOT NULL, " + TURN_X + " INT NULL, " + TURN_Y + " INT NULL, " +
            "PRIMARY KEY(" + GAME_ID + ", " + TURN_NUMBER + "), FOREIGN KEY (" + GAME_ID + ") REFERENCES " + GAME_ID + "(" + ID + ")" +
            "ON DELETE CASCADE ON UPDATE CASCADE);";

    private static final String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + "(" + ID + " INTEGER, " + GOOGLE_TOKEN +
            " TEXT NOT NULL UNIQUE, " + USER_TYPE + " INT NOT NULL," + NICKNAME_STR + "TEXT NOT NULL, " + NICKNAME_ID +
            " TEXT NOT NULL, " + COLOR + " INT UNSIGNED NOT NULL, " + INVITATION_TARGET + " INTEGER NULL, PRIMARY KEY (" + ID +
            "), FOREIGN KEY (" + INVITATION_TARGET + ") REFERENCES " + USER_TABLE + " (" + ID +
            ") ON DELETE CASCADE ON UPDATE CASCADE);";

    private static DBOpenHelper instance;

    private static final String DROP_GAME_TABLE = "DROP TABLE IF EXISTS " + GAME_TABLE + ";";
    private static final String DROP_TURN_TABLE = "DROP TABLE IF EXISTS " + TURN_TABLE + ";";
    private static final String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + USER_TABLE + ";";

    private static final Class<?>[] playerClasses = {HumanPlayer.class, AIPlayer.class};

    public synchronized static DBOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBOpenHelper(context);
        }
        return instance;
    }

    private DBOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_GAME_TABLE);
        db.execSQL(CREATE_TURN_TABLE);
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_GAME_TABLE);
        db.execSQL(DROP_TURN_TABLE);
        db.execSQL(DROP_USER_TABLE);
        onCreate(db);
    }

    public void addGame(Game game) {
        ContentValues cv = new ContentValues();
        long gameId = new SecureRandom().nextLong();
        cv.put(GAME_STATUS, game.isFinished() ? GameStatus.FINISHED.ordinal() : GameStatus.RUNNING.ordinal());
        cv.put(PLAYER_CROSSES, game.getCrossPlayer().getId());
        cv.put(PLAYER_ZERO, game.getZeroPlayer().getId());
        cv.put(ID, gameId);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        cv.put(GAME_DATE, formattedDate);
        getWritableDatabase().insert(GAME_TABLE, null, cv);

        ArrayList<GameEvent> eventHistory = game.getGameLogic().getEventHistory();
        for (int i = 0; i < eventHistory.size(); i++) {
            cv = new ContentValues();
            cv.put(GAME_ID, gameId);
            cv.put(TURN_NUMBER, i);
            GameEvent event = eventHistory.get(i);
            cv.put(TURN_X, event.getTurnX());
            cv.put(TURN_Y, event.getTurnY());
            cv.put(TURN_TYPE, event.getEventTypeAsInt());
            getWritableDatabase().insert(TURN_TABLE, null, cv);
        }
    }

    public Game getActiveGame() {
        Cursor gameCursor = getReadableDatabase().rawQuery(GET_ACTIVE_GAME, null);
        Cursor turnsCursor = getReadableDatabase().rawQuery(GET_ACTIVE_GAME_TURNS, null);

        Log.d("DBHelper", "Loading active game: found " + gameCursor.getCount() + " games and " + turnsCursor.getCount() + " turns");

        Game game = getGameFromCursors(gameCursor, turnsCursor);
        gameCursor.close();
        turnsCursor.close();
        deleteActiveGame();
        return game;
    }

    public void deleteActiveGame() {
        getWritableDatabase().execSQL(DELETE_ACTIVE_GAME_TURNS);
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
            history.add(cursor.getLong(0) + ";" + cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return history;
    }

    private Game getGameFromCursors(Cursor gameCursor, Cursor turnsCursor) {
        if (!gameCursor.moveToFirst()) {
            return null;
        }
        turnsCursor.moveToFirst(); //no need to check it since game may have 0 turns
        Player cross = null, zero = null;
        try {
            cross = (Player) playerClasses[gameCursor.getInt(0)].getMethod("deserialize", long.class, GameLogic.PlayerFigure.class).
                    invoke(null, gameCursor.getLong(0), GameLogic.PlayerFigure.CROSS);
            zero = (Player) playerClasses[gameCursor.getInt(0)].getMethod("deserialize", long.class, GameLogic.PlayerFigure.class).
                    invoke(null, gameCursor.getLong(1), GameLogic.PlayerFigure.ZERO);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ArrayList<GameEvent> turns = new ArrayList<>();
        while (!turnsCursor.isAfterLast()) {
            turns.add(GameEvent.deserialize(turnsCursor.getInt(0), turnsCursor.getInt(1), turnsCursor.getInt(2)));
            turnsCursor.moveToNext();
        }

        return Game.deserializeGame(cross, zero, GameLogic.deserialize(turns));
    }

    public Game getGameById(long id) {
        String[] queryArgs = new String[] {Long.toString(id)};
        Cursor gameCursor = getReadableDatabase().rawQuery(GET_GAME_BY_ID, queryArgs);
        Cursor turnsCursor = getReadableDatabase().rawQuery(GET_TURNS_BY_GAME_ID, queryArgs);
        Log.d("DBHelper", "Loading game by id: found " + gameCursor.getCount() + " games and " + turnsCursor.getCount() + " turns");
        Game game = getGameFromCursors(gameCursor, turnsCursor);
        gameCursor.close();
        turnsCursor.close();
        return game;
    }
}
