package guilds;

import java.util.LinkedList;

public class ServerList extends LinkedList<Server> {

    private static LinkedList<Server> servers;

    public ServerList(){
        servers = new LinkedList<>();
    }

    public LinkedList<Server> toList(){
        return servers;
    }

    public Server get(int index){
        return servers.get(index);
    }

    public int size(){
        return servers.size();
    }

    public void addServer(Server server){
        servers.add(server);
    }
}
