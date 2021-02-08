package database;

import guilds.Server;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import players.Player;
import players.PlayerList;
import raids.Raid;
import raids.RaidList;
import runtime.StatusUpdater;

import java.sql.*;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseHandler {

    public static final Logger logger = LogManager.getLogger(DatabaseHandler.class);
    public static Connection connection;


    /**
     * Connects to the local database instance as a given user.
     * @param userName The name of the user saved in the database
     * @param password The password of the respective user, if they don't have one, set 'null'
     * @return True if the connection to the DB-server has been established successfully, otherwise false.
     */
    public static boolean setupDatabaseConnection(String userName, String password) {
        logger.warn("connecting to db as '{}'@localhost...",
                userName);
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost/", userName, password);
            logger.info("connected to db !");
            return true;
        } catch (SQLException exception) {
            connection = null;
            logger.error("failed connecting to db <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            StatusUpdater.addException();
            return false;
        }
    }

    /**
     * Chooses a given schema from the database that was connected to before
     * @param schemaName Name of the schema
     */
    public static boolean setSchema(String schemaName) {
        logger.warn("setting schema for database to: '{}'", schemaName);
        if (connection == null) {
            logger.warn("there is no established connection!");
            StatusUpdater.addException();
            return false;
        }
        try {
            connection.createStatement().execute("USE " + schemaName);
            logger.info("successfully set to schema '{}'", schemaName);
            return true;
        } catch (SQLException exception) {
            logger.error("failed setting db to schema '{}' <{}> {}",
                    schemaName,
                    exception.getMessage(),
                    exception.getStackTrace());
            StatusUpdater.addException();
            return false;
        }
    }

    /**
     * Allows to add a {@link Player} to the database. If the players unique id exists in the database already, the
     * player will be updated instead.
     * @param player A player from a {@link Raid} to be added to the database or used to update his current existence
     *               (name, damage, attacks).
     */
    private static void add(Player player) {
        try {
            if(!containsPlayer(player)){
                String statementString = "INSERT INTO players (id, name, totaldamage, totalattacks) VALUES ('" +
                        player.getId() + "', '" + player.getByteName() + "', " + player.getDamage() + ", " + player.getAttacks() + ");";
                logger.debug("created new PLAYER '{}'", player.getRealName());
                executeStatement(statementString);
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
                } else {
                    logger.warn("failed to update PLAYER '{}' ({})",
                            existingPlayer.getRealName(),
                            existingPlayer.getId());
                }
            }
        } catch (SQLException exception){
            StatusUpdater.addException();
            logger.error("could not check for PLAYER '{}' ({}) in db, nothing was altered ~source~ {}: {}",
                    player.getRealName(),
                    player.getId(),
                    exception.getMessage(),
                    exception.getStackTrace());
        }
    }


    /**
     * Adds a {@link Raid} to the database. This will only work if the raids unique identifier, their name, is not
     * saved within the database yet.
     * @param raid The Raid that should be added to the database.
     */
    public static void add(Raid raid) {
        try {
            if (!containsRaid(raid)) {
                String statementString = "INSERT INTO raids (date, tier, stage, attempt, raid_name, clan_name) VALUES('" +
                        raid.getDate() + "', " + raid.getTier() + ", " + raid.getStage() + ", " + raid.getTries() + ", '" +
                        raid.getName() + "', '" + raid.getClanName() + "');";
                logger.info("added RAID '{}' to db", raid.getName());
                executeStatement(statementString);
            } else {
                logger.info("RAID '{}' already exists in db, nothing was added", raid.getName());
            }
        } catch (SQLException exception){
            StatusUpdater.addException();
            logger.error("could not check for raid: '{}' in db, nothing was added", raid.getName());
        }
    }


    /**
     * Adds the partaking of a {@link Player} in a {@link Raid} to the database. This will save their
     * damage and attacks with reference to the player and the raid respectively. This method does not check
     * for duplicates!
     * @param raid The raid in the database to which the participation will be added.
     * @param player The player whose statistics will be added to a raids participations.
     * @return True if the participation was added successfully. Otherwise returns false.
     */
    public static boolean add(Raid raid, Player player){
        add(player);
        String statementString = "INSERT INTO participations (attacks, damage, player_id, raid_id) VALUES(" +
                player.getAttacks() + ", " + player.getDamage() + ", '" + player.getId() + "', '" + raid.getName() + "');";
        return executeStatement(statementString);
    }


    /**
     * Checks if a certain {@link Raid} can be found inside of the database.
     * This is done via the unique identifier of a raid, their name.
     * @param raid The raid whose existence will be checked for.
     * @return True if the raid already exists in the database, otherwise false.
     * @throws SQLException Thrown if there was a problem to execute the query looking for raids.
     */
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


    /**
     * Checks if a certain {@link Server} exists in the database. This is done via their (hopefully) unique name.
     * @param server The server to be checked for.
     * @return True if the database contains any raid mapped to the respective server.
     * @throws SQLException Thrown if there was a problem to execute the query looking for raids.
     */
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


    /**
     * Pulls all the raids and their players of one {@link Server} from the database via the raids and participations table.
     * The players will then be parsed into {@link Player} and added to a {@link PlayerList}.
     * This list is then added to the respective {@link Raid} which in return is added to a {@link RaidList}.
     * @param guild The guild of a server whose raids shall be pulled.
     * @return A list of all raids including all players.
     */
    public static RaidList getRaids(Guild guild){
        RaidList list = new RaidList();
        String name = guild.getName().length() < 5 ?
                (guild.getName() + "aaaa").substring(0,5) : guild.getName();
        try {
            ResultSet raidSet = connection.createStatement().executeQuery(
                    "SELECT * FROM raids WHERE clan_name='" + name + "'");
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
                while (playerSet.next()){
                    Player player = new Player(playerSet.getString("name"),
                            playerSet.getString("id"),
                            playerSet.getInt("attacks"),
                            playerSet.getInt("damage"));
                    logger.debug("got PLAYER: {}", player.toString());
                    raid.addPlayer(player);
                }
                logger.debug("found {} PLAYERS in DB for RAID '{}'",
                        raid.getPlayers().size(),
                        raid.getName());
            }
            logger.debug("found {} RAIDS in DB", list.size());
            return list;
        } catch (SQLException exception) {
            StatusUpdater.addException();
            logger.debug("could not fetch raids from db <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            return list;
        }
    }


    /**
     * Pulls all the players of a {@link Server} from the database and totals them (damage and attacks) in one {@link PlayerList}.
     * @param guild The reference to the server of which the players should be pulled.
     * @return A list of all players totalling their damage and attacks from all participations.
     */
    public static PlayerList getPlayers(Guild guild) {
        PlayerList list = new PlayerList();
        String name = guild.getName().length() < 5 ?
                (guild.getName() + "aaaa").substring(0,5) : guild.getName();
        try {
            String sqlString = "SELECT name, damage, id, attacks FROM players AS players INNER JOIN participations AS participations ON " +
                    "players.id=participations.player_id INNER JOIN raids AS raids ON raids.raid_name = participations.raid_id WHERE raids.clan_name='" +
                    name + "'";
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
            StatusUpdater.addException();
            logger.error("failed fetching PLAYERS from db <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            return null;
        }
    }


    /**
     * Checks if the database contains a certain {@link Player}. This is determined by checking for their unique id.
     * @param player The player to be checked for.
     * @return True if a player with the same id as the given player is found.
     * @throws SQLException Thrown if there was a problem with the execution of the query looking for players.
     */
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


    /**
     * Helper method to execute SQL statements more easily.
     * @param sqlString The SQL command that is to be executed in form of a complete String.
     * @return True if the command was executed successfully, otherwise false.
     */
    public static boolean executeStatement(String sqlString){
        try {
            Statement statement = connection.createStatement();
            statement.execute(sqlString);
            logger.debug("successfully executed '{}'", sqlString);
            return true;
        } catch (SQLException exception) {
            StatusUpdater.addException();
            logger.error("trouble executing '{}'; fault: {}: {}", sqlString, exception.getMessage(), exception.getStackTrace());
            return false;
        }
    }


    /**
     * Updates a given {@link Player} in the database. This does not check if the player actually exists.
     * @param player The player whose stats (name, attacks, damage) will be updated.
     * @return True if the player was updated as expected, otherwise false.
     */
    private static boolean updatePlayer(Player player) {
        try {
            String statementString = "UPDATE players SET name='" + player.getRealName() +
                    "', totalattacks=" + player.getAttacks() + ", totaldamage=" +
                    player.getDamage() + " WHERE id='" + player.getId() + "'";
            connection.createStatement().execute(statementString);
            System.out.println(statementString);
            return true;
        } catch (SQLException exception) {
            StatusUpdater.addException();
            logger.debug("failed updating PLAYER '{}' ({}), ~source~ {}",
                    player.getRealName(),
                    player.getId(),
                    exception.getStackTrace());
            return false;
        }
    }

}
