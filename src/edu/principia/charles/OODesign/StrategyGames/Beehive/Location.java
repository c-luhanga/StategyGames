package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.io.Serializable;

/**
 * Location Class Development Guide for Beehive Game
 * 
 * Overview:
 * The Location class encapsulates the row and column indices of a cell within
 * the
 * Beehive game's hexagonal board grid. This class is pivotal for managing cell
 * positions
 * and facilitating the calculation of adjacency and bridging relationships
 * between cells.
 * 
 * Requirements:
 * - The class must accurately represent the position of a cell on the hexagonal
 * grid.
 * - It should provide functionality to compare locations, compute adjacency,
 * and identify
 * bridging positions which are critical for game logic related to move
 * validation and
 * win condition checking.
 * - The class should be designed to easily integrate with the Cell class and
 * any
 * game logic requiring positional calculations.
 * 
 * Member Variables:
 * - row: An integer representing the row index of the cell on the game board.
 * - col: An integer representing the column index of the cell on the game
 * board.
 * 
 * Constructor:
 * - Accepts row and column parameters to initialize a Location instance.
 * 
 * Methods:
 * - equals(Location other): Determines if this Location is equal to another,
 * based on row and column values.
 * - hashCode(): Generates a hash code for a Location, ensuring that Locations
 * with the same row and column have the same hash code (important for use in
 * collections like HashSet or HashMap).
 * - isAdjacent(Location other): Determines if another Location is adjacent to
 * this one,
 * considering the hexagonal grid geometry. Adjacent cells share an edge.
 * - calculateDistance(Location other): Calculates the distance between this
 * Location
 * and another, which could be useful for determining bridging and adjacency in
 * more
 * complex scenarios.
 * - toString(): Returns a string representation of the Location, typically
 * indicating
 * its row and column (useful for debugging and logging).
 * 
 * Additional Considerations:
 * - The implementation of adjacency and distance calculations must account for
 * the
 * hexagonal layout of the board, which differs from traditional square grids.
 * - This class should be immutable; once a Location instance is created, its
 * row
 * and column should not change. This immutability ensures that Location
 * instances
 * can be safely used as keys in collections like HashMaps or elements in Sets
 * without unexpected behavior.
 * - Consider providing static methods or a LocationFactory for generating
 * Locations
 * based on game-specific rules, such as identifying all adjacent or bridged
 * positions relative to a given Location.
 */

public class Location implements Serializable {
    public int row;
    public int col;
    private static final int rows = 11;
    private static final int cols = 11;
    public static final Location[] ADJACENT_OFFSETS = {
            new Location(-1, 0), // North
            new Location(-1, 1), // Northeast
            new Location(0, 1), // Southeast
            new Location(1, 0), // South
            new Location(1, -1), // Southwest
            new Location(0, -1) // Northwest
    };

    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean equals(Location other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Location location = (Location) other;
        return row == location.row && col == location.col;
    }

    public int hashCode() {
        return row * 31 + col;
    }

    public Location add(Location other) {
        return new Location(row + other.row, col + other.col);
    }

    public boolean inBounds() {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}
