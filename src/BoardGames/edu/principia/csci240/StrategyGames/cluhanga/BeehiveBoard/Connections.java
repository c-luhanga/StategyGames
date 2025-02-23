package boardgames.edu.principia.csci240.strategygames.cluhanga.BeehiveBoard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connections Class Development Guide for Beehive Game
 * 
 * Overview:
 * The Connections class is responsible for managing the direct adjacency
 * relationships between cells on the Beehive game board. It should efficiently
 * determine if two cells are adjacent (share an edge) on the hexagonal grid.
 * 
 * Requirements:
 * - Efficiently store and query adjacency relationships between cells
 * identified by their Location.
 * - Support the game logic by providing methods to check if two cells are
 * adjacent.
 * 
 * Member Variables:
 * - adjacencyMap: A data structure (e.g., HashMap<Location, List<Location>>)
 * that maps each cell location to a list of locations that are adjacent to it.
 * 
 * Methods:
 * - addConnection(Location from, Location to): Adds a bidirectional adjacency
 * relationship between two cell locations.
 * - isAdjacent(Location from, Location to): Returns true if the 'to' Location
 * is adjacent to the 'from' Location.
 * - getAdjacentLocations(Location from): Returns a list of Locations that are
 * adjacent to the 'from' Location.
 * 
 * Additional Considerations:
 * - Consider the uniqueness of the hexagonal grid when implementing adjacency.
 * Unlike square grids, hexagonal grids have six neighbors for each cell.
 * - Ensure the adjacencyMap is symmetric; if A is adjacent to B, then B must
 * also be adjacent to A.
 */

public class Connections {
    private Map<Location, List<Location>> adjacencyMap;
    private int gridHeight;
    private int gridWidth;

    public Connections() {
        adjacencyMap = new HashMap<>();
    }

    public void addConnection(Location from, Location to) {
        adjacencyMap.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        adjacencyMap.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
    }

    public boolean isAdjacent(Location from, Location to) {
        int dx = Math.abs(from.getCol() - to.getCol());
        int dy = Math.abs(from.getRow() - to.getRow());

        // Directly adjacent vertically (same column)
        boolean vertical = dx == 0 && dy == 1;

        // Adjacent in the same row or diagonal (for flat-topped)
        boolean horizontalOrDiagonal = dy == 0 && dx == 1;

        return vertical || horizontalOrDiagonal;
    }

    public List<Location> getAdjacentLocations(Location from) {
        List<Location> adjacentLocations = new ArrayList<>();
        int[][] directions = {
                { +1, 0 }, { +1, -1 }, { 0, -1 },
                { -1, 0 }, { -1, +1 }, { 0, +1 }
        };

        for (int[] dir : directions) {
            int adjQ = from.getCol() + dir[0]; // Adjust column based on direction
            int adjR = from.getRow() + dir[1]; // Adjust row based on direction

            // Assuming a method isValidLocation(adjQ, adjR) checks if the location is
            // within the grid bounds
            if (isValidLocation(adjQ, adjR)) {
                adjacentLocations.add(new Location(adjQ, adjR));
            }
        }

        return adjacentLocations;
    }

    private boolean isValidLocation(int q, int r) {
        // Implement this method to check if the location is within the grid bounds.
        // For example:
        return q >= 0 && q < gridWidth && r >= 0 && r < gridHeight;
    }

    /**
     * Determines if a "bridge" exists between two given cell locations on the
     * Beehive game board.
     * A bridge in the Beehive game represents a special type of connection that
     * allows players to
     * extend their reach beyond directly adjacent cells, following specific game
     * rules.
     *
     * The concept of a bridge is central to adding strategic depth to the game,
     * allowing players
     * to form connections between cells that are not directly adjacent but can be
     * "leapt" over
     * according to certain conditions. This method must carefully evaluate the
     * positions of the
     * two cells in question and the state of any intermediary cells to determine if
     * a valid bridge
     * exists according to the rules.
     *
     * Parameters:
     * 
     * @param from The starting Location of the potential bridge. This location must
     *             contain a cell
     *             belonging to the current player.
     * @param to   The ending Location of the potential bridge. Like the starting
     *             location, this must
     *             also be occupied by the current player's cell.
     * 
     *             Returns:
     * @return boolean True if a valid bridge exists between the 'from' and 'to'
     *         locations,
     *         false otherwise.
     *
     *         Logic:
     *         1. Determine the linear distance and direction between the 'from' and
     *         'to' locations.
     *         Bridges can only form in straight lines on the hexagonal grid.
     *         2. Check the intermediary cells (those that lie on the line between
     *         'from' and 'to') for
     *         their occupancy state. A bridge is typically considered valid if
     *         these cells are empty,
     *         allowing for a "leap" over them.
     *         3. Evaluate any game-specific conditions for bridge formation. For
     *         example, the game might
     *         require that only certain cells can serve as bridge endpoints, or
     *         there may be restrictions
     *         based on the current state of the game (e.g., bridges cannot form
     *         over cells containing
     *         certain types of pieces).
     *         4. Return true if all conditions for a bridge are met, indicating
     *         that the 'from' and 'to'
     *         locations are connected by a bridge. Otherwise, return false.
     *
     *         Usage:
     *         This method is utilized in the gameplay logic to check for player
     *         moves that attempt to
     *         establish a bridge, as well as in the evaluation of game state for
     *         win conditions or strategic
     *         advantages. Implementing efficient and accurate bridge detection is
     *         critical for ensuring
     *         fair play and the integrity of the game's strategic elements.
     *
     *         Note: The exact implementation details and conditions for bridge
     *         formation should be adapted
     *         to fit the specific rules and mechanics of the Beehive game as
     *         defined by its designers.
     */
    public boolean isBridge(Location cell, Location other) {
        // Check if the two locations are aligned either vertically, horizontally, or
        // diagonally
        int dx = other.getCol() - cell.getCol();
        int dy = other.getRow() - cell.getRow();

        // Normalize dx and dy for direction checking
        int dirX = Integer.compare(dx, 0);
        int dirY = Integer.compare(dy, 0);

        // Check if the cells are aligned either vertically, horizontally, or diagonally
        // and there's exactly one cell space between them
        if (Math.abs(dx) == 2 || Math.abs(dy) == 2) {
            // Calculate midpoints based on the direction
            // Adjust these calculations based on your game's grid and rules
            int midX = cell.getCol() + dirX;
            int midY = cell.getRow() + dirY;
            Location midLoc = new Location(midX, midY);

            // Assume the mid-cell for a potential bridge needs to be empty
            return adjacencyMap.get(midLoc) == null || adjacencyMap.get(midLoc).isEmpty(); // Check if the midLoc is
                                                                                           // empty
        }

        return false;
    }

    public void addBridge(Location cell, Location other) {
        if (!isBridge(cell, other)) {
            throw new IllegalArgumentException("The two locations do not form a bridge");
        }

        // Add the 'other' location to the 'cell' location's list of bridged locations
        List<Location> cellBridgedLocations = adjacencyMap.get(cell);
        if (cellBridgedLocations == null) {
            cellBridgedLocations = new ArrayList<>();
            adjacencyMap.put(cell, cellBridgedLocations);
        }
        cellBridgedLocations.add(other);

        // Add the 'cell' location to the 'other' location's list of bridged locations
        List<Location> otherBridgedLocations = adjacencyMap.get(other);
        if (otherBridgedLocations == null) {
            otherBridgedLocations = new ArrayList<>();
            adjacencyMap.put(other, otherBridgedLocations);
        }
        otherBridgedLocations.add(cell);
    }

    public boolean isConnected(Location from, Location to) {
        if (isAdjacent(from, to)) {
            return true;
        }

        // Check if the two locations are connected through a bridge
        if (isBridge(from, to)) {
            return true;
        }

        // If the code reaches this point, the two locations are not connected
        return false;
    }

    public Location[] getBridgedLocations(Location cell, Location other) {
        List<Location> bridgedLocations = new ArrayList<>();

        // Based on your definition, let's check for a straight line bridge.
        // This assumes bridges can be formed in straight lines either vertically,
        // horizontally, or in diagonal lines. Adjust logic as per your game rules.

        int dx = other.getCol() - cell.getCol();
        int dy = other.getRow() - cell.getRow();

        // Normalize dx and dy for direction checking
        int dirX = Integer.compare(dx, 0);
        int dirY = Integer.compare(dy, 0);

        // Check if the cells are aligned either vertically, horizontally, or diagonally
        // and there's exactly one cell space between them
        if (Math.abs(dx) <= 2 && Math.abs(dy) <= 2 && !(dx == 0 && dy == 0)) {
            // Calculate midpoints based on the direction
            // Adjust these calculations based on your game's grid and rules
            if (Math.abs(dx) == 2 || Math.abs(dy) == 2) {
                // Assume the mid-cell for a potential bridge needs to be empty
                int midX1 = cell.getCol() + dirX;
                int midY1 = cell.getRow() + dirY;
                Location midLoc1 = new Location(midX1, midY1);

                int midX2 = midX1 + dirX; // For cases where you check the second cell in the same direction
                int midY2 = midY1 + dirY;
                Location midLoc2 = (Math.abs(dx) == 2 && Math.abs(dy) == 2) ? new Location(midX2, midY2) : midLoc1;

                if (midLoc1.isEmpty() && (midLoc2.isEmpty() || midLoc1.equals(midLoc2))) { // Assuming Location has an
                                                                                           // isEmpty method
                    bridgedLocations.add(midLoc1);
                    if (!midLoc1.equals(midLoc2)) {
                        bridgedLocations.add(midLoc2);
                    }
                }
            }
        }

        // Convert the list to an array before returning
        return bridgedLocations.toArray(new Location[0]);
    }

    // junit test for is bridge
    public static void main(String[] args) {

        Connections connections = new Connections();
        Location cell = new Location(1, 1);
        Location other = new Location(3, 1);

        // Add the necessary cells to form a bridge
        connections.addConnection(new Location(1, 1), new Location(2, 1));
        connections.addConnection(new Location(2, 1), new Location(3, 1));

        // Test the isBridge method
        System.out.println(connections.isBridge(cell, other)); // Expected: true

        // Test with locations that do not form a bridge
        Location cell2 = new Location(0, 0);
        Location other2 = new Location(11, 2);
        System.out.println(connections.isBridge(cell2, other2)); // Expected: false
    }

}
