package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class BridgeGroup implements Serializable {
    public List<Cell> cells;
    List<Connections> connections = new ArrayList<Connections>();
    public int minRow, maxCol, maxRow, minCol;
    int span = 0;
    private int owner;
    private Cell cell1, cell2;

    public BridgeGroup() {
        cells = new ArrayList<>();
        minRow = Integer.MAX_VALUE;
        maxRow = Integer.MIN_VALUE;
        minCol = Integer.MAX_VALUE;
        maxCol = Integer.MIN_VALUE;
    }

    public int getOwner() {
        return owner;
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

    public int getWeight(int player) {
        return cells.size() * getSpan(player);
    }

    public void add(Cell c) {
        // Check if the cell is already part of this group
        if (this.cells.contains(c)) {
            return;
        }

        // Add the cell to this group
        this.cells.add(c);
        c.group = this; // Update the cell's group reference to this group

        // Update span for player 1 (horizontal span) or player 2 (vertical span)
        int oldSpan = (c.player == 1) ? this.maxCol - this.minCol + 1 : this.maxRow - this.minRow + 1;
        if (c.player == 1) {
            this.minCol = Math.min(this.minCol, c.col);
            this.maxCol = Math.max(this.maxCol, c.col);
        } else {
            this.minRow = Math.min(this.minRow, c.row);
            this.maxRow = Math.max(this.maxRow, c.row);
        }
        int newSpan = (c.player == 1) ? this.maxCol - this.minCol + 1 : this.maxRow - this.minRow + 1;
        if (newSpan > oldSpan) {
            this.span++;
        }

        // Merge groups if the cell is part of another group
        if (c.group != null && c.group != this) {
            List<Cell> otherCells = new ArrayList<>(c.group.cells); // Avoid ConcurrentModificationException
            for (Cell otherCell : otherCells) {
                this.add(otherCell); // This will also update the cell's group reference
            }
        }
    }

    public void formBridge(Cell c1, Cell c2) {
        connections.add(new Connections(c1, c2)); // Adds a new connection to the list
    }

    public void setOwner(int newOwner) {
        this.owner = newOwner;
    }

    public int getSpan(int player) {
        if (player == 1) {
            return maxCol - minCol + 1;
        } else {
            return maxRow - minRow + 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Bridge Group:\n");
        for (Cell cell : cells) {
            sb.append(cell.toString()).append("\n");
        }
        return sb.toString();
    }
}