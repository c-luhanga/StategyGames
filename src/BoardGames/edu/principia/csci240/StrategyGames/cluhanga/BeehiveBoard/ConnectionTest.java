package BoardGames.edu.principia.csci240.StrategyGames.cluhanga.BeehiveBoard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionsTest {
    @Test
    void testIsBridge() {
        assertEquals(1, 1);
        Connections connections = new Connections();
        Location cell = new Location(1, 1);
        Location other = new Location(3, 1);

        // Add the necessary cells to form a bridge
        connections.addConnection(new Location(1, 1), new Location(2, 1));
        connections.addConnection(new Location(2, 1), new Location(3, 1));

        // Test the isBridge method
        assertTrue(connections.isBridge(cell, other), "Expected a bridge between the two locations");

        // Test with locations that do not form a bridge
        Location cell2 = new Location(0, 0);
        Location other2 = new Location(2, 2);
        assertFalse(connections.isBridge(cell2, other2), "Expected no bridge between the two locations");
    }

    void testIsAdjacent() {
        assertEquals(1, 1);
        Connections connections = new Connections();
        Location cell = new Location(1, 1);
        Location other = new Location(2, 1);

        // Test the isAdjacent method
        assertTrue(connections.isAdjacent(cell, other), "Expected the two locations to be adjacent");

        // Test with locations that are not adjacent
        Location cell2 = new Location(0, 0);
        Location other2 = new Location(2, 2);
        assertFalse(connections.isAdjacent(cell2, other2), "Expected the two locations to not be adjacent");
    }
}
