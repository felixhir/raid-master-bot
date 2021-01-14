package handler;

import objects.Player;
import objects.Raid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseHandler extends Thread {

    private final String DATABASE_USER;

    public static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    public int timer;
    public static Connection connection;


    public DatabaseHandler() throws SQLException {
        String DATABASE_NAME = System.getenv("DB_NAME");
        this.DATABASE_USER = System.getenv("DB_USER");
        timer = 1000;
        logger.warn("connecting to database with user '{}'@'localhost'", DATABASE_USER);
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost/", DATABASE_USER, null);
            logger.info("successfully established a connection");
        } catch (SQLException exception) {
            logger.error("failed to connect to database after {} seconds", timer);
            this.start();
        }
        connection.createStatement().execute("USE " + DATABASE_NAME);
    }


    @SuppressWarnings("unused")
    final
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            logger.info("entering DB connection loop");

            while (true) {
                try {
                    connection = DriverManager.getConnection("jdbc:mariadb://localhost/", DATABASE_USER, null);
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

    public static boolean add(Player player) {
        String statementString = "INSERT INTO players (id, name, totaldamage, totalattacks) VALUES ('" +
        player.getId() + "', '" + player.getName() + "', " + player.getDamage() + ", " + player.getAttacks() + ");";

        return executeStatement(statementString);
    }

    public static boolean add(Raid raid) {
        try {
            if (!containsRaid(raid)) {
                String statementString = "INSERT INTO raids (date, tier, stage, attempt, raid_name, clan_name) VALUES('" +
                        raid.getDate() + "', " + raid.getTier() + ", " + raid.getStage() + ", " + raid.getTries() + ", '" +
                        raid.getRaidName() + "', '" + raid.getClanName() + "');";
                return executeStatement(statementString);
            }
            logger.info("raid: '{}' already exists in db, nothing was added", raid.getRaidName());
            return false;
        } catch (SQLException exception){
            logger.error("could not check for raid: '{}' in database, nothing was added", raid.getRaidName());
            return false;
        }
    }

    private static boolean executeStatement(String sqlString){
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

    public static boolean containsRaid(Raid raid) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM raids");
        while(resultSet.next()){
            if(resultSet.getString("raid_name").equals(raid.getRaidName())){
                return true;
            }
        }
        return false;
    }

    public static boolean containsServer(String name) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT clan_name FROM raids");
        while(resultSet.next()){
            if(resultSet.getString("clan_name").equals(name)){
                return true;
            }
        }
        return false;
    }
}
