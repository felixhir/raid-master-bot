package guilds;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raids.Raid;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class ServerTest {

    private Server server;
    private Raid raid;

    @BeforeEach
    void setUp() {
        server = new Server("clanclan");
        raid = new Raid(1,2,3,"clanclan", new Date(10-10-2020));
    }

    @Test
    public void getRaidName_formsRaidName() {
        String result = server.getRaidName("1\\_2\\_3");

        assertEquals("10203clanc", result);
    }
}