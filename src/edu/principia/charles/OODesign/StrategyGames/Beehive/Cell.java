package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Cell implements Serializable {
    public int row;
    public int col;
    public Location loc; // Assuming Location is a class that holds row and col.
    public int player; // 0 for empty, 1 for Player 1, 2 for Player 2
    public int bridgeGroup; // Group ID for bridge connection management
    int owner; // Owner of the cell
    public BridgeGroup group; // Reference to the group the cell belongs to
    public List<Location> adjacent; // List of adjacent locations for this cell
    public List<Location> bridged; // List of locations that are reachable through bridging
    public boolean visited = false; // Tracks whether the cell has been visited in traversal algorithms
    public boolean printed = false; // Used for debugging or printing purposes
    public Location[] adjOffsets = new Location[] { new Location(0, 1), new Location(1, 0), new Location(1, -1),
            new Location(0, -1), new Location(-1, 0), new Location(-1, 1) };
    public Location[] bridgeOffsets = new Location[] { new Location(-1, -1), new Location(-1, 2),
            new Location(-2, 1),
            new Location(1, -2), new Location(1, 1), new Location(2, -1) };

    public Cell(int r, int c) {
        player = 0;
        row = r;
        col = c;
        loc = new Location(r, c);
        bridgeGroup = -1;
        adjacent = new ArrayList<Location>();
        bridged = new ArrayList<Location>();
    }

    public void computeAdj() {
        adjacent.clear();
        for (int i = 0; i < 6; i++) {
            Location loc = new Location(row, col).add(adjOffsets[i]);
            if (loc.inBounds()) {
                adjacent.add(loc);
            }
        }
    }

    public boolean contains(Cell o) {
        return (row == o.row && col == o.col);
    }

    public List<Location> computeAdjLoc() {
        List<Location> adj = new ArrayList<Location>();
        for (int i = 0; i < 6; i++) {
            Location loc = new Location(row, col).add(adjOffsets[i]);
            if (loc.inBounds()) {
                adj.add(loc);
            }
        }
        return adj;
    }

    public void computeBridge() {
        bridged.clear();
        for (int i = 0; i < 6; i++) {
            Location loc = new Location(row, col).add(bridgeOffsets[i]);
            if (loc.inBounds()) {
                bridged.add(loc);
            }
        }
    }

    public void setOwner(int newOwner) {
        owner = newOwner;
        System.out.println(
                "Cell at " + (loc.row + 1) + ", " + (loc.col + 1) + " is now owned by player " + newOwner + ".");
        if (newOwner == 1) {
            player = 1;
        } else if (newOwner == 2) {
            player = 2;
        }
    }

    public int getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "Cell at (" + loc.row + ", " + loc.col + ")";
    }
}