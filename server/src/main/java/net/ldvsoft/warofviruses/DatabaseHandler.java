package net.ldvsoft.warofviruses;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by ldvsoft on 09.12.15.
 */
public class DatabaseHandler {
    protected MysqlDataSource dataSource;
    protected WarOfVirusesServer server;

    public DatabaseHandler(WarOfVirusesServer server) throws SQLException {
        this.server = server;

        dataSource = new MysqlDataSource();
        dataSource.setServerName  (server.getSetting("db.server"  ));
        dataSource.setDatabaseName(server.getSetting("db.name"    ));
        dataSource.setUser        (server.getSetting("db.user"    ));
        dataSource.setPassword    (server.getSetting("db.password"));
    }
}
