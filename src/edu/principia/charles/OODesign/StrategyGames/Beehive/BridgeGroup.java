package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * BridgeGroup Class Development Guide for Beehive Game
 * 
 * Overview:
 * The BridgeGroup class represents a set of cells that are connected either
 * directly (adjacent) or through a bridge (two empty cells between them). This
 * concept is essential for evaluating potential paths and win conditions in the
 * Beehive game.
 * 
 * Requirements:
 * - Manage groups of connected cells, considering both adjacency and bridging.
 * - Support queries related to the game's win condition, such as checking if a
 * BridgeGroup spans from one side of the board to the other.
 * 
 * Member Variables:
 * - cells: A collection (e.g., HashSet<Location>) of cell locations that belong
 * to the BridgeGroup.
 * - gameBoard: A reference to the BeehiveBoard or a similar structure that
 * allows checking the state of specific cells for adjacency and bridging.
 * 
 * Methods:
 * - addCell(Location cell): Adds a cell to the BridgeGroup, updating
 * connections and bridges as necessary.
 * - isConnected(Location from, Location to): Determines if two locations are
 * connected within the group, either directly or through a bridge.
 * - spansBoard(): Checks if the BridgeGroup spans from one designated side of
 * the board to the other, contributing to win condition evaluation.
 * 
 * Additional Considerations:
 * - The implementation needs to efficiently update and query connections and
 * bridges within the group, especially as new cells are added.
 * - Consider using additional data structures or algorithms to quickly
 * determine the connectivity and reachability of cells within the group.
 * - Bridging relationships might require a more complex check than direct
 * adjacency, especially considering the requirement for intermediate cells to
 * be empty.
 */

public class BridgeGroup {
    public List<Cell> cells = new ArrayList<>();
    private List<Connections> connections = new ArrayList<>();
    private int minRow = Integer.MAX_VALUE;
    private int maxRow = Integer.MIN_VALUE;
    private int minCol = Integer.MAX_VALUE;
    private int maxCol = Integer.MIN_VALUE;
    private int span = 0;
    private int owner;
    private Cell cell1;
    private Cell cell2;

    public BridgeGroup() {
        cells = new ArrayList<Cell>();
        minRow = Integer.MAX_VALUE;
        maxRow = Integer.MIN_VALUE;
        minCol = Integer.MAX_VALUE;
        maxCol = Integer.MIN_VALUE;
    }

    public Cell getOtherCell(Cell cell) {
        if (cell.equals(cell1)) {
            return cell2;
        } else if (cell.equals(cell2)) {
            return cell1;
        } else {
            throw new IllegalArgumentException("Cell not found in group");
        }
    }

    public int getOwner() {
        return owner;
    }

    public void formBridge(Cell cell1, Cell cell2) {
        // Add the cells to the group
        add(cell1);
        add(cell2);

        // Create a connection between the cells
        connections.add(new Connections(cell1, cell2));
    }

    public void addCell(Cell cell) {
        // Add the cell to the group
        cells.add(cell);

        // Check if the new cell forms a bridge with any existing cell
        for (Cell existingCell : cells) {
            if (isBridge(cell, existingCell)) {
                // Form a bridge between the new cell and the existing cell
                formBridge(cell, existingCell);
            }
        }
    }

    private boolean isBridge(Cell cell, Cell existingCell) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBridge'");
    }

    public void add(Cell cell) {
        if (cells.contains(cell))
            return;

        cells.add(cell);
        cell.setGroup(this); // Assume setGroup method in Cell class to handle setting group

        updateSpanForPlayer(cell);
        mergeGroupsIfNeeded(cell);
    }

    private void updateSpanForPlayer(Cell cell) {
        if (cell.getOwner() == 1) {
            updateHorizontalSpan(cell);
        } else if (cell.getOwner() == 2) {
            updateVerticalSpan(cell);
        }
    }

    private void updateHorizontalSpan(Cell cell) {
        minCol = Math.min(minCol, cell.getCol());
        maxCol = Math.max(maxCol, cell.getCol());
        span = Math.max(span, maxCol - minCol + 1);
    }

    private void updateVerticalSpan(Cell cell) {
        minRow = Math.min(minRow, cell.getRow());
        maxRow = Math.max(maxRow, cell.getRow());
        span = Math.max(span, maxRow - minRow + 1);
    }

    private void mergeGroupsIfNeeded(Cell cell) {
        BridgeGroup otherGroup = cell.getGroup();
        if (otherGroup != null && otherGroup != this) {
            for (Cell otherCell : new ArrayList<>(otherGroup.cells)) {
                this.add(otherCell); // Recursive call will handle updating group references
            }
        }
    }

    public int getSpan(int player) {
        return player == 1 ? maxCol - minCol + 1 : maxRow - minRow + 1;
    }

    public int getWeight(int player) {
        return cells.size() * getSpan(player);
    }

}
