package BoardGames.edu.principia.csci240.StrategyGames.cluhanga.BeehiveBoard;

import java.util.HashSet;
import java.util.LinkedList;
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
    private Set<Location> cells;
    private BeehiveBoard gameBoard;
    private int player;
    private static int nextId = 1;
    private int id;

    public BridgeGroup(BeehiveBoard gameBoard) {
        this.gameBoard = gameBoard;
        cells = new HashSet<>();
        player = 0;
        this.id = nextId++;
    }

    public BridgeGroup() {
        cells = new HashSet<>();
        this.gameBoard = new BeehiveBoard();
        player = 0;
        this.id = nextId++;
    }

    public int getId() {
        return id;
    }

    public void addCell(Location cell) {
        Set<BridgeGroup> connectedGroups = new HashSet<>();
        for (Location other : gameBoard.getConnections().getAdjacentLocations(cell)) {
            BridgeGroup otherGroup = gameBoard.getGroup(other);
            if (otherGroup != null) {
                connectedGroups.add(otherGroup);
            }
        }
        if (player == 0) {
            player = gameBoard.getState(cell);
        }

        // If the cell is connected to more than one group, merge them
        if (connectedGroups.size() > 1) {
            BridgeGroup mergedGroup = new BridgeGroup(gameBoard);
            for (BridgeGroup group : connectedGroups) {
                mergedGroup.merge(group);
                gameBoard.removeGroup(group);
            }
            gameBoard.addGroup(mergedGroup);
        }

        // Add the cell to the group
        cells.add(cell);
        updateConnections(cell);
    }

    void merge(BridgeGroup group) {
        cells.addAll(group.cells);
        for (Location cell : group.cells) {
            gameBoard.setGroup(cell, this); // Update the gameBoard to reference the current group
            updateConnections(cell);
        }

        gameBoard.removeGroup(group);
    }

    private void updateConnections(Location cell) {
        // Check for adjacency with other cells in the group
        for (Location other : cells) {
            if (gameBoard.getConnections().isAdjacent(cell, other)) {
                // Update adjacency
                gameBoard.getConnections().addConnection(cell, other);
            }
            if (gameBoard.getConnections().isBridge(cell, other)) {
                // Update bridging
                gameBoard.getConnections().addBridge(cell, other);
            }
            for (Location bridged : gameBoard.getConnections().getBridgedLocations(cell, other)) {
                cells.add(bridged);
            }
        }
    }

    public boolean isConnected(Location from, Location to) {
        return gameBoard.getConnections().isConnected(from, to);
    }

    public boolean spansBoard() {
        // Check if the group spans from one side to the other
        for (Location cell : cells) {
            if (gameBoard.isPlayerOneSide(cell) && spansToOppositeSide(cell, 1)) {
                return true;
            }
            if (gameBoard.isPlayerOneSide(cell) && spansToOppositeSide(cell, 2)) {
                return true;
            }
        }
        return false;
    }

    private boolean spansToOppositeSide(Location start, int player) {
        Set<Location> visited = new HashSet<>();
        Queue<Location> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            visited.add(current);

            if (gameBoard.isOppositeSide(current, player)) {
                return true;
            }

            for (Location neighbor : gameBoard.getConnections().getAdjacentLocations(current)) {
                if (!visited.contains(neighbor) && cells.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    public boolean contains(Location cell) {
        return cells.contains(cell);
    }

    public int size() {
        return cells.size();
    }

    public int getPlayer() {
        return player;
    }

    public void add(Location location) {
        cells.add(location);
    }

    public Location[] getCells() {
        return cells.toArray(new Location[0]);
    }

    @Override
    public String toString() {
        return "Group ID: " + id + ", Player: " + getPlayer() + ", Size: " + size() + ", Cells: " + cells;
    }

    public void setPlayer(int player) {
        this.player = player;
    }
}
