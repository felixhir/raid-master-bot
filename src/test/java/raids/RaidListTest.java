package raids;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class RaidListTest {

    private RaidList raids;
    private Raid testRaid;
    private Raid testRaidTwo;

    @BeforeEach
    void setUp() {
        raids = new RaidList();
        testRaid = new Raid(1,1,1,"test_clan", new Date(-1));
        testRaidTwo = new Raid(2,2,2,"test_clan", new Date(1612980726));
    }

    @Test
    void getList_returnsEmptyListOrSizeOne() {
        LinkedList<Raid> result = raids;

        assertTrue(result.isEmpty());

        raids.add(testRaid);
        result = raids;

        assertFalse(result.isEmpty());
    }

    @Test
    void size_returnsZero() {
        int result = raids.size();

        assertEquals(0, result);
    }

    @Test
    void size_returnsTwo() {
        raids.add(testRaid);
        raids.add(testRaidTwo);

        int result = raids.size();

        assertEquals(2, result);
    }

    @Test
    void get_returnsTestRaid() {
        raids.add(testRaidTwo);
        raids.add(testRaid);

        Raid result = raids.get(1);

        assertEquals(testRaidTwo, result);
    }

    @Test
    void add_canAddTwoRaids() {
        boolean resultOne = raids.add(testRaid);
        boolean resultTwo = raids.add(testRaidTwo);

        assertTrue(resultOne);
        assertTrue(resultTwo);
    }

    @Test
    void add_raidsAreAddedByDate() {
        raids.add(testRaidTwo);
        raids.add(testRaid);
        Raid raid = raids.getRecentRaid();

        assertEquals(testRaidTwo, raid);
    }

    @Test
    void toString_returnsAllRaidNames() {
        raids.add(testRaid);
        raids.add(testRaidTwo);
        String result = raids.toString();
        String expected = "List contains: '10101', '20202'";

        assertEquals(expected, result);
    }

    @Test
    void containsRaid_raidIsntContained() {
        raids.add(testRaid);
        boolean result = raids.containsRaid(testRaidTwo.getName());

        assertFalse(result);
    }

    @Test
    void containsRaid_raidIsContained() {
        raids.add(testRaid);
        boolean result = raids.containsRaid(testRaid.getName());

        assertTrue(result);
    }
}