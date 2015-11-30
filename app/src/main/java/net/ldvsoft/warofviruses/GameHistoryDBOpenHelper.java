package net.ldvsoft.warofviruses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Created by Сева on 04.11.2015.
 */
public class GameHistoryDBOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 4;
    private static final String DB_NAME = "gameHistoryDB";
    private static final String GAME_HISTORY_TABLE = "gameHistoryTable";
    private static final String ID = "id";
    private static final String GAME_DATA = "gameData";
    private static final String IS_FINISHED = "isFinished";

    private static final String CREATE_TABLE = "CREATE TABLE " + GAME_HISTORY_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            GAME_DATA + " BLOB, " + IS_FINISHED + " INTEGER);";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + GAME_HISTORY_TABLE + ";";

    private static final String ADD_GAME = "INSERT INTO " + GAME_HISTORY_TABLE + "(" + GAME_DATA + ", " + IS_FINISHED +
            ") VALUES (?, ?);";

    private static final String GET_ACTIVE_GAME = "SELECT " + GAME_DATA + " FROM " + GAME_HISTORY_TABLE + " WHERE " + IS_FINISHED + " = 0;";

    private static final String DELETE_ACTIVE_GAME = "DELETE FROM " + GAME_HISTORY_TABLE + " WHERE " + IS_FINISHED + " = 0;";
    public GameHistoryDBOpenHelper(Context context) {// TODO: remove saving at SD card in release
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
        getWritableDatabase().insert(GAME_HISTORY_TABLE, null, cv);
    }

    public byte[] getSerializedActiveGame() {
        Cursor cursor = getReadableDatabase().rawQuery(GET_ACTIVE_GAME, null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        byte[] data = cursor.getBlob(0);
        getWritableDatabase().rawQuery(DELETE_ACTIVE_GAME, null);
        return data;
    }

    public void deleteActiveGame() {
        getWritableDatabase().execSQL(DELETE_ACTIVE_GAME, null);
    }
}
