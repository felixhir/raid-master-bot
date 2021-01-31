package database;

import guilds.Server;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import players.Player;
import players.PlayerList;
import raids.Raid;
import raids.RaidList;

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
            logger.error("failed to connect to database <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
        }
        connection.createStatement().execute("USE " + DATABASE_NAME);
    }


    public static boolean add(Player player) {
        try {
            if(!containsPlayer(player)){
                String statementString = "INSERT INTO players (id, name, totaldamage, totalattacks) VALUES ('" +
                        player.getId() + "', '" + player.getByteName() + "', " + player.getDamage() + ", " + player.getAttacks() + ");";
                logger.debug("created new PLAYER '{}'", player.getRealName());
                return executeStatement(statementString);
            } else {
                logger.info("PLAYER '{}' ({}) already exists in db, updating them",
                        player.getRealName(),
                        player.getId());
                ResultSet playerSet = connection.createStatement().executeQuery("SELECT * FROM players WHERE id='" + player.getId() + "';");
                playerSet.next();
                Player existingPlayer = new Player(playerSet.getString("name"),
                        playerSet.getString("id"),
                        playerSet.getInt("totalattacks"),
                        playerSet.getInt("totaldamage"));
                existingPlayer.addDamage(player.getDamage());
                existingPlayer.addAttacks(player.getAttacks());
                existingPlayer.setRealName(player.getByteName());
                if(updatePlayer(existingPlayer)) {
                    logger.debug("updated PLAYER '{}' ({})",
                            existingPlayer.getRealName(),
                            existingPlayer.getId());
                    return true;
                } else {
                    logger.warn("failed to update PLAYER '{}' ({})",
                            existingPlayer.getRealName(),
                            existingPlayer.getId());
                    return false;
                }
            }
        } catch (SQLException exception){
            logger.error("could not check for PLAYER '{}' ({}) in db, nothing was altered ~source~ {}: {}",
                    player.getRealName(),
                    player.getId(),
                    exception.getMessage(),
                    exception.getStackTrace());
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

    public static boolean add(Raid raid, Player player){
        add(player);
        String statementString = "INSERT INTO participations (attacks, damage, player_id, raid_id) VALUES(" +
                player.getAttacks() + ", " + player.getDamage() + ", '" + player.getId() + "', '" + raid.getName() + "');";
        return executeStatement(statementString);
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

    public static RaidList getRaids(){
        RaidList list = new RaidList();
        try {
            ResultSet raidSet = connection.createStatement().executeQuery("SELECT * FROM raids");
            logger.debug("found {} RAIDS in DB", raidSet.getFetchSize());
            while (raidSet.next()) {
                Raid raid = new Raid(raidSet.getInt("tier"),
                        raidSet.getInt("stage"),
                        raidSet.getInt("attempt"),
                        raidSet.getString("clan_name"),
                        raidSet.getDate("date"));
                list.add(raid);
                String sqlString = "SELECT name, damage, id, attacks FROM players AS players INNER JOIN participations AS participations ON " +
                        "players.id=participations.player_id INNER JOIN raids AS raids ON raids.raid_name = participations.raid_id WHERE raids.raid_name='" +
                        raid.getName() + "'";
                ResultSet playerSet = connection.createStatement().executeQuery(sqlString);
                logger.debug("found {} PLAYERS in DB for RAID '{}'",
                        playerSet.getFetchSize(),
                        raid.getName());
                while (playerSet.next()){
                    Player player = new Player(playerSet.getString("name"),
                            playerSet.getString("id"),
                            playerSet.getInt("attacks"),
                            playerSet.getInt("damage"));
                    logger.debug("got PLAYER: {}", player.toString());
                    raid.addPlayer(player);
                }
            }
            return list;
        } catch (SQLException exception) {
            logger.debug("could not fetch raids from db <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            return list;
        }
    }

    public static PlayerList getPlayers(Guild guild) {
        PlayerList list = new PlayerList();
        try {
            String sqlString = "SELECT name, damage, id, attacks FROM players AS players INNER JOIN participations AS participations ON " +
                    "players.id=participations.player_id INNER JOIN raids AS raids ON raids.raid_name = participations.raid_id WHERE raids.clan_name='" +
                    guild.getName() + "'";
            ResultSet playerSet = connection.createStatement().executeQuery(sqlString);
            while(playerSet.next()) {
                Player player = new Player(playerSet.getString("name"),
                        playerSet.getString("id"),
                        playerSet.getInt("attacks"),
                        playerSet.getInt("damage"));
                list.add(player);
            }
            logger.debug("created new PLAYERLIST with size {}", list.size());
            return list;
        } catch (SQLException exception) {
            logger.error("failed fetching PLAYERS from db <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            return null;
        }
    }

    private static boolean containsPlayer(Player player) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT id FROM players");
        while(resultSet.next()){
            if(resultSet.getString("id").equals(player.getId())){
                logger.debug("found PLAYER '{}' ({}) in db",
                        player.getRealName(),
                        player.getId());
                return true;
            }
        }
        logger.debug("could not find PLAYER '{}' ({}) in db",
                player.getRealName(),
                player.getId());
        return false;
    }

    private static boolean executeStatement(String sqlString){
        try {
            Statement statement = connection.createStatement();
            statement.execute(sqlString);
            logger.debug("successfully executed '{}'", sqlString);
            return true;
        } catch (SQLException exception) {
            logger.error("trouble executing '{}'; fault: {}: {}", sqlString, exception.getMessage(), exception.getStackTrace());
            return false;
        }
    }

    private static boolean updatePlayer(Player player) {
        try {
            String statementString = "UPDATE players SET name='" + player.getRealName() +
                    "', totalattacks=" + player.getAttacks() + ", totaldamage=" +
                    player.getDamage() + " WHERE id='" + player.getId() + "'";
            connection.createStatement().execute(statementString);
            System.out.println(statementString);
            return true;
        } catch (SQLException exception) {
            logger.debug("failed updating PLAYER '{}' ({}), ~source~ {}",
                    player.getRealName(),
                    player.getId(),
                    exception.getStackTrace());
            return false;
        }
    }

}
