package handler;

import objects.Server;
import objects.ServerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class GuildHandler {

    private static ServerList servers;
    public static final Logger logger = LogManager.getLogger(GuildHandler.class.getName());

    public GuildHandler(){
        servers = new ServerList();
        logger.info("{} has been instantiated", GuildHandler.class);
    }

    public void addServer(Server server){
        servers.addServer(server);
    }

    public LinkedList<Server> getServers(){
        return servers.toList();
    }

    public Server getServerByName(String name){
        Server server;
        server = null;
        for(Server s: servers.toList()) if (s.getName().equals(name)) server = s;
        return server;
    }

}
