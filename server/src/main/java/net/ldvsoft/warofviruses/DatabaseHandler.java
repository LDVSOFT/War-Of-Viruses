package net.ldvsoft.warofviruses;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ldvsoft on 09.12.15.
 */
public class DatabaseHandler implements DBProvider {
    private Logger logger = Logger.getLogger(DatabaseHandler.class.getName());

    protected MysqlDataSource dataSource;
    protected WarOfVirusesServer server;

    public DatabaseHandler(WarOfVirusesServer server) throws SQLException {
        this.server = server;

        dataSource = new MysqlDataSource();
        dataSource.setServerName(server.getSetting("db.server"));
        dataSource.setDatabaseName(server.getSetting("db.name"));
        dataSource.setUser(server.getSetting("db.user"));
        dataSource.setPassword(server.getSetting("db.password"));
    }

    @Override
    public void addGame(Game game) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                long id = game.getGameId();
                {
                    GameStatus status = game.isFinished() ? GameStatus.FINISHED : GameStatus.RUNNING;

                    PreparedStatement addGameStatement = connection.prepareStatement(ADD_GAME);
                    addGameStatement.setLong(1, id);
                    addGameStatement.setLong(2, game.getCrossPlayer().getId());
                    addGameStatement.setLong(3, game.getZeroPlayer().getId());
                    addGameStatement.setInt(4, status.ordinal());

                    addGameStatement.execute();
                }
                {
                    ArrayList<GameEvent> events = game.getGameLogic().getEventHistory();

                    PreparedStatement addGameTurnStatement = connection.prepareStatement(ADD_GAME_TURNS);
                    addGameTurnStatement.setLong(1, id);
                    for (int i = 0; i != events.size(); i++) {
                        addGameTurnStatement.setInt(2, i);

                        GameEvent event = events.get(i);
                        addGameTurnStatement.setInt(3, event.getEventTypeAsInt());
                        addGameTurnStatement.setInt(4, event.getTurnX());
                        addGameTurnStatement.setInt(5, event.getTurnY());

                        addGameTurnStatement.execute();
                    }
                }
                connection.commit();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save game.", e);
        }
    }

    @Override
    public void deleteActiveGame() {
        throw new UnsupportedOperationException("deleteActiveGame");
    }

    @Override
    public ArrayList<String> getGameHistory() {
        throw new UnsupportedOperationException("getGameHistory");
    }

    @Override
    public Game getGameById(long id) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement getGameStatement = connection.prepareStatement(GET_GAME_BY_ID);
                getGameStatement.setLong(1, id);
                ResultSet game = getGameStatement.executeQuery();
                if (!game.first())
                    return null;
                //TODO
                Player cross = null, zero = null;

                PreparedStatement getGameTurnsStatement = connection.prepareStatement(GET_TURNS_BY_GAME_ID);
                getGameTurnsStatement.setLong(1, id);
                ResultSet events = getGameTurnsStatement.executeQuery();
                events.beforeFirst();
                ArrayList<GameEvent> eventList = new ArrayList<>();
                while (!events.isAfterLast()) {
                    eventList.add(GameEvent.deserialize(events.getInt(1), events.getInt(2), events.getInt(3)));
                    events.next();
                }

                return Game.deserializeGame(id, cross, zero, GameLogic.deserialize(eventList));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load game.", e);
            return null;
        }
    }
}
