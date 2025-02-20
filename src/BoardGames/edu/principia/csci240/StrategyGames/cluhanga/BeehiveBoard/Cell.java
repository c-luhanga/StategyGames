package BoardGames.edu.principia.csci240.StrategyGames.cluhanga.BeehiveBoard;

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
    private int state;
    private int row;
    private int col;
    private List<Cell> adjacentCells;
    private List<Cell> bridgedCells;
    private BridgeGroup group;

    public Cell() {
        this.state = 0;
        this.row = 0;
        this.col = 0;
        this.adjacentCells = new ArrayList<>();
        this.bridgedCells = new ArrayList<>();
    }

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.state = 0;
        this.adjacentCells = new ArrayList<>();
        this.bridgedCells = new ArrayList<>();
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void addAdjacentCell(Cell cell) {
        adjacentCells.add(cell);
    }

    public void addBridgedCell(Cell cell) {
        bridgedCells.add(cell);
    }

    public List<Cell> getAdjacentCells() {
        return adjacentCells;
    }

    public List<Cell> getBridgedCells() {
        return bridgedCells;
    }

    public boolean isEmpty() {
        return state == 0;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public BridgeGroup getGroup() {
        return this.group;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cell at (").append(row).append(", ").append(col).append(")\n");
        sb.append("State: ").append(state == 0 ? "Empty" : (state == 1 ? "Player 1" : "Player 2")).append("\n");
        sb.append("Adjacent cells: ").append(adjacentCells.size()).append("\n");
        sb.append("Bridged cells: ").append(bridgedCells.size()).append("\n");
        return sb.toString();
    }

    public void setGroup(BridgeGroup bridgeGroup) {
        // Set the group to which this cell belongs
        this.group = bridgeGroup;
    }

    public Object getLocation() {
        return new int[] { row, col };
    }
}
