package handler;

import objects.Player;
import objects.Raid;
import objects.Server;
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
        logger.warn("connecting to db with user '{}'@'localhost'", DATABASE_USER);
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
        try {
            if(!containsPlayer(player)){
                String statementString = "INSERT INTO players (id, name, totaldamage, totalattacks) VALUES ('" +
                        player.getId() + "', '" + player.getName() + "', " + player.getDamage() + ", " + player.getAttacks() + ");";
                logger.info("added PLAYER '{}' ({}) to db",
                        player.getName(),
                        player.getId());
                return executeStatement(statementString);
            } else {
                logger.info("PLAYER '{}' ({}) already exists in db, updating them",
                        player.getName(),
                        player.getId());
                return true;
            }
        } catch (SQLException exception){
            logger.error("could not check for PLAYER '{}' ({}) in db, nothing was altered",
                    player.getName(),
                    player.getId());
            return false;
        }
    }

    public static boolean add(Raid raid) {
        try {
            if (!containsRaid(raid)) {
                String statementString = "INSERT INTO raids (date, tier, stage, attempt, raid_name, clan_name) VALUES('" +
                        raid.getDate() + "', " + raid.getTier() + ", " + raid.getStage() + ", " + raid.getTries() + ", '" +
                        raid.getName() + "', '" + raid.getClanName() + "');";
                logger.info("added RAID '{}' to db", raid.getName());
                return executeStatement(statementString);
            } else {
                logger.info("RAID '{}' already exists in db, nothing was added", raid.getName());
                return false;
            }
        } catch (SQLException exception){
            logger.error("could not check for raid: '{}' in db, nothing was added", raid.getName());
            return false;
        }
    }

    public static boolean containsRaid(Raid raid) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM raids");
        while(resultSet.next()){
            if(resultSet.getString("raid_name").equals(raid.getName())){
                logger.debug("found RAID '{}' in db", raid.getName());
                return true;
            }
        }
        logger.debug("could not find RAID '{}' in db", raid.getName());
        return false;
    }

    public static boolean containsServer(Server server) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT clan_name FROM raids");
        while(resultSet.next()){
            if(resultSet.getString("clan_name").equals(server.getName())){
                logger.debug("found SERVER '{}' in db", server.getName());
                return true;
            }
        }
        logger.debug("could not find SERVER '{}' in db", server.getName());
        return false;
    }

    private static boolean containsPlayer(Player player) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT id FROM players");
        while(resultSet.next()){
            if(resultSet.getString("id").equals(player.getId())){
                logger.debug("found PLAYER '{}' ({}) in db",
                        player.getName(),
                        player.getId());
                return true;
            }
        }
        logger.debug("could not find PLAYER '{}' ({}) in db",
                player.getName(),
                player.getId());
        return false;
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

}
