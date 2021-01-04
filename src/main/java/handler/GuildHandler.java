package handler;

import objects.Server;
import objects.ServerList;

import java.util.LinkedList;

public class GuildHandler {

    private static ServerList servers;

    public GuildHandler(){
        servers = new ServerList();
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
