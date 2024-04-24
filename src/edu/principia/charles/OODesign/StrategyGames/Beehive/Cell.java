package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.util.ArrayList;
import java.util.List;

/**
 * Cell Class Development Guide for Beehive Game
 * 
 * Overview:
 * The Cell class represents a single hexagonal cell on the Beehive game board.
 * Each cell has a state indicating whether it is empty, occupied by player 1,
 * or occupied by player 2. Additionally, cells need to be aware of their
 * neighbors
 * to support game logic related to forming connected paths.
 * 
 * Requirements:
 * - The class must encapsulate the state of the cell and its position on the
 * board.
 * - It should provide methods to determine cell adjacency and bridging for path
 * finding and win condition checks.
 * - Cells are critical in identifying valid moves, especially when implementing
 * the swap rule and checking for game-ending conditions.
 * 
 * Member Variables:
 * - state: An integer or enum representing the current state of the cell
 * (empty, player 1's stone, or player 2's stone).
 * - row: An integer storing the cell's row position on the game board.
 * - col: An integer storing the cell's column position on the game board.
 * - adjacentCells: A list of cells that are adjacent to this cell.
 * Adjacency is defined as sharing an edge in the hexagonal grid.
 * - bridgedCells: A list of cells that are connected to this cell via a bridge.
 * A bridge exists between two cells if they are two steps apart with exactly
 * two intermediate cells that are empty.
 * 
 * Constructor:
 * - Initializes a cell with its position (row and column) and sets its initial
 * state to empty.
 * - Optionally, the constructor can also initialize the lists for adjacent and
 * bridged cells,
 * though these could be set post-construction as the board is being
 * initialized.
 * 
 * Methods:
 * - setState(int state): Sets the state of the cell.
 * - getState(): Returns the current state of the cell.
 * - addAdjacentCell(Cell cell): Adds a cell to the list of adjacent cells.
 * - addBridgedCell(Cell cell): Adds a cell to the list of bridged cells.
 * - getAdjacentCells(): Returns the list of cells adjacent to this cell.
 * - getBridgedCells(): Returns the list of cells that form bridges with this
 * cell.
 * - isOccupied(): Returns true if the cell is occupied by either player 1 or
 * player 2.
 * - isEmpty(): Returns true if the cell is empty.
 * 
 * Additional Considerations:
 * - Consider implementing methods to support querying the cell's position
 * (getRow, getCol).
 * - Efficiency in maintaining and accessing adjacent and bridged cells is
 * crucial for
 * performance, particularly in path finding and win condition checks.
 * - This class should be designed with immutability in mind for the cell's
 * position
 * to ensure consistency throughout the game's lifecycle.
 */

public class Cell {
    private int row;
    private int col;
    private Location loc;
    public int player = 0;
    private int bridgeGroup = -1;
    public BridgeGroup group;
    public List<Location> adjacent = new ArrayList<>();
    public List<Location> bridged = new ArrayList<>();
    private int owner;
    public boolean visited = false;
    public boolean printed = false;

    private static final Location[] ADJACENT_OFFSETS = {
            new Location(0, 1), new Location(1, 0), new Location(1, -1),
            new Location(0, -1), new Location(-1, 0), new Location(-1, 1)
    };

    private static final Location[] BRIDGE_OFFSETS = {
            new Location(-1, -1), new Location(-1, 2), new Location(-2, 1),
            new Location(1, -2), new Location(1, 1), new Location(2, -1)
    };

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.loc = new Location(row, col);
    }

    public void computeAdjacentLocations() {
        adjacent.clear();
        for (Location offset : ADJACENT_OFFSETS) {
            Location adjacentLoc = new Location(row, col).add(offset);
            if (adjacentLoc.inBounds()) {
                adjacent.add(adjacentLoc);
            }
        }
    }

    public List<Location> getAdjacentLocations() {
        List<Location> adjLocs = new ArrayList<>();
        for (Location offset : ADJACENT_OFFSETS) {
            Location adjacentLoc = new Location(row, col).add(offset);
            if (adjacentLoc.inBounds()) {
                adjLocs.add(adjacentLoc);
            }
        }
        return adjLocs;
    }

    public void computeBridgedLocations() {
        bridged.clear();
        for (Location offset : BRIDGE_OFFSETS) {
            Location bridgeLoc = new Location(row, col).add(offset);
            if (bridgeLoc.inBounds()) {
                bridged.add(bridgeLoc);
            }
        }
    }

    public boolean contains(Cell other) {
        return this.row == other.row && this.col == other.col;
    }

    public void setOwner(int owner) {
        this.owner = owner;
        this.player = owner; // Assuming player numbers are the same as owner numbers.
        // System.out.println(String.format("Cell at %d, %d is now owned by player %d.",
        // loc.row + 1, loc.col + 1, owner));
    }

    public int getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("Cell at (%d, %d), State: %s, Adjacent cells: %d, Bridged cells: %d",
                row, col, player == 0 ? "Empty" : "Player " + player, adjacent.size(), bridged.size());
    }

    public void setGroup(BridgeGroup bridgeGroup2) {
        this.group = bridgeGroup2;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public BridgeGroup getGroup() {
        return group;
    }
}
