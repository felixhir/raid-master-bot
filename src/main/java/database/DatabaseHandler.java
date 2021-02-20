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

    private static String user;
    private static String password;
    private static String schema;


    /**
     * Connects to the local database instance as a given user.
     * @param userName The name of the user saved in the database
     * @param pw The password of the respective user, if they don't have one, set 'null'
     * @return True if the connection to the DB-server has been established successfully, otherwise false.
     */
    public static boolean createConnection(String userName, String pw) {
        logger.warn("connecting to db as '{}'@localhost",
                userName);
        user = userName;
        password = pw;
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost/", userName, password);
            logger.debug("connected to db!");
            if (!(schema == null)) setSchema(schema);
            return true;
        } catch (SQLException exception) {
            connection = null;
            logger.fatal("failed connecting to db <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            StatusUpdater.addException();
            return false;
        }
    }


    /**
     * Shuts down the existing connection to the database.
     */
    public static void closeConnection(){
        logger.info("closing down DB connection");
        try {
            connection.close();
        } catch (SQLException exception) {
            logger.error("failed cutting connection <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
        }
    }

    /**
     * Chooses a given schema from the database that was connected to before
     * @param schemaName Name of the schema
     */
    public static boolean setSchema(String schemaName) {
        logger.warn("setting schema for database to: '{}'", schemaName);
        schema = schemaName;
        try {
            if (connection == null || connection.isClosed()) {
                logger.error("there is no established connection, creating a new one...");
                createConnection(user, password);
                StatusUpdater.addException();
            }
            connection.createStatement().execute("USE " + schemaName);
            logger.debug("successfully set to schema '{}'", schemaName);
            return true;
        } catch (SQLException exception) {
            logger.fatal("failed setting db to schema '{}' <{}> {}",
                    schemaName,
                    exception.getMessage(),
                    exception.getStackTrace());
            StatusUpdater.addException();
            return false;
        }
    }

    /**
     * Allows to add a {@link Player} to the database. This will save their name and id and if the id exists update the name
     * @param player A player from a {@link Raid} to be added to the database or used to update his current existence
     *               (name).
     */
    private static void add(Player player) {
        try {
            if(connection.isClosed()) createConnection(user, password);
            if(!containsPlayer(player)){
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO players (id, name) VALUES " +
                        "(?, ?)");
                statement.setString(1, player.getId());
                statement.setString(2, player.getByteName());
                statement.execute();
                logger.debug("created new PLAYER '{}'", player.getRealName());
            } else {
                logger.debug("PLAYER '{}' ({}) already exists in db, updating name",
                        player.getRealName(),
                        player.getId());
                PreparedStatement statement = connection.prepareStatement("UPDATE players SET name=? WHERE id=?");
                statement.setString(1, player.getByteName());
                statement.setString(2, player.getId());
                statement.execute();
            }
        } catch (SQLException exception){
            StatusUpdater.addException();
            logger.error("there was an error adding PLAYER '{}' ({}) to db <{}> {}",
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
    public static boolean add(Raid raid) {
        try {
            if(connection.isClosed()) createConnection(user, password);
            if (!containsRaid(raid)) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO raids (date, tier, stage, attempt, clan_name) VALUES" +
                        "(?, ?, ?, ?, ?)");
                statement.setDate(1, raid.getDate());
                statement.setInt(2, raid.getTier());
                statement.setInt(3, raid.getStage());
                statement.setInt(4, raid.getTries());
                statement.setString(5, raid.getClanName());
                logger.debug("added RAID '{}' to db", raid.getName());
                statement.execute();
                return true;
            } else {
                logger.debug("RAID '{}' already exists in db, nothing was added", raid.getName());
                return false;
            }
        } catch (SQLException exception){
            StatusUpdater.addException();
            logger.error("could not check for raid: '{}' in db, nothing was added", raid.getName());
            return false;
        }
    }


    /**
     * Adds the partaking of a {@link Player} in a {@link Raid} to the database. This will save their
     * damage and attacks with reference to the player and the raid respectively. This method does not check
     * for duplicates and always uses the latest raid.
     * @param player The player whose statistics will be added to a raids participations.
     * @return True if the participation was added successfully. Otherwise returns false.
     */
    public static boolean addToRaid(Player player){
        add(player);
        int id = 0;
        try {
            if(connection.isClosed()) createConnection(user, password);
            ResultSet raids = connection.createStatement().executeQuery("SELECT id FROM raids");
            while (raids.next()) {
                id = raids.getInt("id");
            }
        } catch (SQLException exception) {
            logger.error("failed getting raid id <{}> {}",
                    exception.getMessage(),
                    exception.getStackTrace());
            return false;
        }

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO participations (attacks, damage, player_id, raid_id) VALUES" +
                    "(?, ?, ?, ?)");
            statement.setInt(1, player.getAttacks());
            statement.setInt(2, player.getDamage());
            statement.setString(3, player.getId());
            statement.setInt(4, id);
            statement.execute();
            return true;
        } catch (SQLException exception) {
            logger.error("failed adding participation of player '{}' to raid #{} <{}> {}",
                    player.getRealName(),
                    id,
                    exception.getMessage(),
                    exception.getStackTrace());
            return false;
        }
    }


    /**
     * Checks if a certain {@link Raid} can be found inside of the database.
     * This is done via the unique identifier of a raid, their name.
     * @param raid The raid whose existence will be checked for.
     * @return True if the raid already exists in the database, otherwise false.
     * @throws SQLException Thrown if there was a problem to execute the query looking for raids.
     */
    public static boolean containsRaid(Raid raid) throws SQLException {
        if(connection.isClosed()) createConnection(user, password);
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM raids");
        while(resultSet.next()){
            if(resultSet.getString("clan_name").equals(raid.getClanName()) &&
                    resultSet.getInt("tier") == raid.getTier() &&
                    resultSet.getInt("stage") == raid.getStage() &&
                    resultSet.getInt("attempt") == raid.getTries()){
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
        if(connection.isClosed()) createConnection(user, password);
        PreparedStatement statement = connection.prepareStatement("SELECT name FROM servers");
        ResultSet resultSet = statement.executeQuery();
        while(resultSet.next()){
            if(resultSet.getString("name").equals(server.getName())){
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
        try {
            if(connection.isClosed()) createConnection(user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM raids WHERE clan_name=?");
            statement.setString(1, guild.getName());
            ResultSet raidSet = statement.executeQuery();
            while (raidSet.next()) {
                Raid raid = new Raid(raidSet.getInt("tier"),
                        raidSet.getInt("stage"),
                        raidSet.getInt("attempt"),
                        raidSet.getString("clan_name"),
                        raidSet.getDate("date"));
                list.add(raid);
                PreparedStatement statementPlayers = connection.prepareStatement("SELECT players.id, players.name, attacks, damage" +
                        " FROM players INNER JOIN participations p on players.id = p.player_id" +
                        " INNER JOIN raids r on p.raid_id = r.id WHERE r.clan_name=?");
                statementPlayers.setString(1, guild.getName());
                ResultSet playerSet = statementPlayers.executeQuery();
                while (playerSet.next()){
                    Player player = new Player(playerSet.getString("name"),
                            playerSet.getString("id"),
                            playerSet.getInt("attacks"),
                            playerSet.getInt("damage"));
                    raid.addPlayer(player);
                }
                logger.debug("found {} PLAYERS in DB for RAID '{}'",
                        raid.getPlayers().size(),
                        raid.getName());
            }
            logger.info("found {} RAIDS in DB for SERVER '{}'",
                    list.size(),
                    guild.getName());
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
        try {
            if(connection.isClosed()) createConnection(user, password);
            PreparedStatement statement  = connection.prepareStatement("SELECT players.id, name, attacks, damage" +
                    " FROM players INNER JOIN participations p on players.id = p.player_id" +
                    " INNER JOIN raids r on p.raid_id = r.id WHERE r.clan_name=?");
            statement.setString(1, guild.getName());
            ResultSet playerSet = statement.executeQuery();
            while(playerSet.next()) {
                Player player = new Player(playerSet.getString("name"),
                        playerSet.getString("id"),
                        playerSet.getInt("attacks"),
                        playerSet.getInt("damage"));
                list.add(player);
            }
            logger.info("created new PLAYERLIST with size {}", list.size());
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
        if(connection.isClosed()) createConnection(user, password);
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
     * Adds a {@link Server} with their name, current prefix and server to the database.
     * @param server The server to be added.
     * @return True if the server was added successfully, otherwise false.
     */
    public static boolean add(Server server) {
        try {
            if(connection.isClosed()) createConnection(user, password);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO servers(name, prefix, afktimer) VALUES (?, ?, ?)");
            statement.setString(1, server.getName());
            statement.setString(2, String.valueOf(server.getPrefix()));
            statement.setInt(3, server.getAfktimer());
            statement.execute();
            logger.info("added SERVER '{}' to DB", server.getName());
            return true;
        } catch (SQLException exception) {
            logger.error("failed to add SERVER '{}' to DB <{}> {}",
                    server.getName(),
                    exception.getMessage(),
                    exception.getStackTrace());
            return false;
        }
    }


    /**
     * Updates a servers afktimer and command prefix in the database.
     * @param server The server to be updated
     * @return True if the server was successfully updated, otherwise false.
     */
    public static boolean updateServer(Server server) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE servers SET afktimer=?, prefix=? WHERE name=?");
            statement.setInt(1, server.getAfktimer());
            statement.setString(2, String.valueOf(server.getPrefix()));
            statement.setString(3, server.getName());
            statement.execute();
            return true;
        } catch (SQLException exception) {
            logger.error("failed updating SERVER '{}' <{}> {}",
                    server.getName(),
                    exception.getMessage(),
                    exception.getStackTrace());
            return false;
        }
    }

    /**
     * Returns the prefix for commands of a certain {@link Server} from the database.
     * @param server The server whose prefix should be returned.
     * @return The prefix of the server as a char. If this was not changed it will always return '!'.
     */
    public static char getServerPrefix(Server server) {
        try {
            if(connection.isClosed()) createConnection(user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT prefix FROM servers WHERE name=?");
            statement.setString(1, server.getName());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString("prefix").toCharArray()[0];
        } catch (SQLException exception) {
            logger.error("failed pulling prefix for SERVER '{}', using default <{}> {}",
                    server.getName(),
                    exception.getMessage(),
                    exception.getStackTrace());
            return '!';
        }
    }

    /**
     * Returns the afktime of a certain {@link Server} from the database.
     * @param server The server whose afktime should be returned.
     * @return The afktime of the server as an integer. If this was not changed it will always return 2.
     */
    public static int getServerAfktime(Server server) {
        try {
            if(connection.isClosed()) createConnection(user, password);
            PreparedStatement statement = connection.prepareStatement("SELECT afktimer FROM servers WHERE name=?");
            statement.setString(1, server.getName());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("afktimer");
        } catch (SQLException exception) {
        logger.error("failed pulling afktime for SERVER '{}', using default <{}> {}",
                server.getName(),
                exception.getMessage(),
                exception.getStackTrace());
        return '2';
        }
    }
}
