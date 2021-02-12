package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseHandlerTest {

    @Test
    void setupDatabaseConnection_failsWithWrongValues() {
        boolean result = DatabaseHandler.createConnection("thisUserDoesntExist", "null");

        assertFalse(result);
    }

    @Test
    void setupDatabaseConnection_succeedsWithCredentials() {
        boolean result = DatabaseHandler.createConnection("testing_user", null);

        assertTrue(result);
    }

    @Test
    void setSchema_schemaDoesntExist() {
        DatabaseHandler.createConnection("testing_user", null);
        boolean result = DatabaseHandler.setSchema("thisSchemaDoesntExist");

        assertFalse(result);
    }

    @Test
    void setSchema_schemaExists() {
        DatabaseHandler.createConnection("testing_user", null);
        boolean result = DatabaseHandler.setSchema("testingschema");

        assertTrue(result);
    }

    @AfterEach
    void tearDown() {
        wipeSchema();
    }

    private void wipeSchema() {
        DatabaseHandler.createConnection("testing_user", null);
        DatabaseHandler.executeStatement("DELETE FROM participations");
        DatabaseHandler.executeStatement("DELETE FROM raids");
        DatabaseHandler.executeStatement("DELETE FROM players");
    }
}