package net.ldvsoft.warofviruses;

import java.util.ArrayList;

/**
 * Created by Сева on 11.12.2015.
 */
public interface DBProvider {
    String GAME_TABLE = "Game";
    String ID = "id";
    String GAME_STATUS = "status";
    String GAME_DATE = "gameDate";
    String PLAYER_ZERO = "playerZero";
    String PLAYER_CROSSES = "playerCrosses";

    String TURN_TABLE = "Turn";
    String GAME_ID = "game";
    String TURN_NUMBER = "turnNo";
    String TURN_TYPE = "type";
    String TURN_X = "x";
    String TURN_Y = "y";
    String USER_TABLE = "User";
    String GOOGLE_TOKEN = "googleToken";
    String USER_TYPE = "userType";
    String NICKNAME_STR = "nicknameStr";
    String NICKNAME_ID = "nicknameID";
    String COLOR = "color";
    String INVITATION_TARGET = "invocationTarget";

    enum GameStatus {RUNNING, FINISHED, DELETED}

    ; //probably not there, in some other class

    String GET_ACTIVE_GAME = "SELECT " + PLAYER_CROSSES + ", " + PLAYER_ZERO + " FROM " + GAME_TABLE +
            " WHERE " + GAME_STATUS + " = " + GameStatus.RUNNING.ordinal() + ";";

    String GET_ACTIVE_GAME_TURNS = "SELECT " + TURN_TYPE + ", " + TURN_X + ", " + TURN_Y +
            " FROM " + TURN_TABLE + " INNER JOIN " + GAME_TABLE + " ON " + GAME_TABLE + "." + ID + " = " + TURN_TABLE + "." + GAME_ID +
            " AND " + GAME_TABLE + "." + GAME_STATUS + " = " + GameStatus.RUNNING.ordinal() + " ORDER BY " + TURN_NUMBER + " ASC;";

    String DELETE_ACTIVE_GAME_TURNS = "DELETE FROM " + TURN_TABLE + " WHERE " + GAME_ID + " IN (" +
            " SELECT " + ID + " FROM " + GAME_TABLE + " WHERE " + ID + " = " + GAME_ID + " AND " + GAME_STATUS + " = " +
            GameStatus.RUNNING.ordinal() + ");";

    String DELETE_ACTIVE_GAME = "DELETE FROM " + GAME_TABLE + " WHERE " + GAME_STATUS +
            " =" + GameStatus.RUNNING.ordinal() + ";";

    String GET_GAME_HISTORY = "SELECT " + ID + ", " + GAME_DATE + " FROM " + GAME_TABLE + " WHERE " +
            GAME_STATUS + " = 1 ORDER BY " + GAME_DATE + " DESC;";

    String GET_GAME_BY_ID = "SELECT " + PLAYER_CROSSES + ", " + PLAYER_ZERO + " FROM " + GAME_TABLE +
            " WHERE " + ID + " = ?;";

    String GET_TURNS_BY_GAME_ID = "SELECT " + TURN_TYPE + ", " + TURN_X + ", " + TURN_Y +
            " FROM " + TURN_TABLE + " WHERE " + GAME_ID + " =?;";

    String ADD_GAME = "INSERT INTEGER " + GAME_TABLE + "(" + ID + ", " + PLAYER_CROSSES + ", " + PLAYER_ZERO +
            ", " + GAME_STATUS + ", " + GAME_DATE + ") VALUES (?, ?, ?, ?, ?);";

    String ADD_GAME_TURNS = "INSERT INTEGER " + TURN_TABLE + "(" + GAME_ID + ", " + TURN_NUMBER + ", " + TURN_TYPE +
            ", " + TURN_X + ", " + TURN_Y + ") VALUES (?, ?, ?, ?, ?);";

    void addGame(Game game);
    void deleteActiveGame();
    ArrayList<String> getGameHistory();
    Game getGameById(long id);
}
