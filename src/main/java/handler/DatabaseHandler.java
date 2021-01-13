package handler;

import objects.Player;
import objects.Raid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseHandler extends Thread {

    private final String databaseUser;

    public static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    public int timer;
    public Connection connection;


    public DatabaseHandler(String databaseUser) throws SQLException {
        this.databaseUser = databaseUser;
        timer = 1000;
        logger.warn("connecting to database with user '{}'@'localhost'", databaseUser);
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost/", "felix", null);
            logger.info("successfully established a connection");
        } catch (SQLException exception) {
            logger.error("failed to connect to database after {} seconds", timer);
            this.start();
        }
        connection.createStatement().execute("USE test");
    }


    @SuppressWarnings("unused")
    final
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            logger.info("entering DB connection loop");

            while (true) {
                try {
                    connection = DriverManager.getConnection("jdbc:mariadb://localhost/", databaseUser, null);
                    return;
                } catch (SQLException minorException) {
                    logger.error("failed to connect to database, reason is '{}': {}",
                            minorException.getMessage(),
                            minorException.getStackTrace());
                    try {
                        //noinspection BusyWait
                        Thread.sleep(timer);
                        logger.info("after {} seconds a new connection attempt is being started", timer);
                    } catch (InterruptedException exception) {
                        logger.error("failed to put thread to sleep");
                    }
                    timer = 2 * timer;
                }
            }
        }
    };

    public boolean addPlayer(Player player) {
        String statementString = "INSERT INTO players (id, name, totaldamage, totalattacks) VALUES ('" +
        player.getId() + "', '" + player.getName() + "', " + player.getDamage() + ", " + player.getAttacks() + ");";

        return executeStatement(statementString);
    }

    public boolean addRaid(Raid raid) {
        String statementString = "INSERT INTO raids (name, stage, tier, tries) VALUES('" +
                raid.getName() + "', " + raid.getStage() + ", " + raid.getTier() + ", " + raid.getTries() + ");";

        return executeStatement(statementString);
    }

    private boolean executeStatement(String sqlString){
        try {
            Statement statement = connection.createStatement();
            statement.execute(sqlString);
            logger.info("successfully executed '{}'", sqlString);
            return true;
        } catch (SQLException exception) {
            logger.error("trouble executing '{}'; fault: {}: {}", sqlString, exception.getMessage(), exception.getStackTrace());
            return false;
        }
    }
}
