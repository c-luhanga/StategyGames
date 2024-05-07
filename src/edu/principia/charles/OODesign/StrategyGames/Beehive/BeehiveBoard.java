package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import edu.principia.OODesign.StrategyGames.Board.Board;

/**
 * BeehiveBoard Class Development Guide with Integrated Components
 * 
 * Overview:
 * BeehiveBoard acts as the central hub for the Beehive game logic, interfacing
 * directly with game-specific classes such as Cell, Location, Connections, and
 * BridgeGroup to manage the game state, enforce rules, and evaluate win
 * conditions on an 11x11 hexagonal grid.
 * 
 * Requirements:
 * - Manage the board's cell states and their interconnections to facilitate
 * game play, ensuring compliance with the game's rules including piece
 * placement and the swap rule.
 * - Utilize the Cell, Location, Connections, and BridgeGroup classes to
 * represent and manipulate the game board's state efficiently.
 * - Implement game logic to check win conditions based on the connectivity of
 * cells across the board.
 * 
 * Member Variables:
 * - cells: A 2D array of Cell objects representing the hexagonal grid of the
 * game board.
 * - currentPlayer: Tracks which player's turn it is, alternating between two
 * players.
 * - connections: An instance of the Connections class to manage adjacency
 * relationships between cells.
 * - bridgeGroups: A list of BridgeGroup objects, each representing a set of
 * connected cells.
 * 
 * Constructor:
 * - Initializes the cells array with empty Cell objects, sets the starting
 * player, and prepares the Connections and BridgeGroup instances.
 * 
 * Key Methods:
 * - applyMove(Move move): Applies a move to the board by updating the relevant
 * Cell state and adjusting the Connections and BridgeGroups accordingly.
 * - isValidMove(Move move): Determines if a proposed move is valid by checking
 * the target Cell's state and compliance with game rules.
 * - checkWin(): Utilizes the BridgeGroup objects to determine if a player has
 * successfully formed a connecting path across the board.
 * - getValidMoves(): Generates and returns a list of all legal moves for the
 * current player, based on the current state of the board and the game's rules.
 * - getCurrentPlayer(): Returns an identifier for the current player.
 * - undoMove(): Reverses the last move made, leveraging the move history to
 * restore the board's previous state.
 * - toString(): Provides a visual representation of the board's current state,
 * useful for debugging or displaying the game in a text-based interface.
 * 
 * Integration with Cell, Location, Connections, and BridgeGroup:
 * - The BeehiveBoard will create and manage Cell objects for each position on
 * the board, using Location objects to track their positions.
 * - The Connections class is used to maintain a record of which cells are
 * adjacent to each other, facilitating the game logic related to piece
 * placement and connectivity.
 * - BridgeGroups are dynamically managed to keep track of connected components
 * on the board, essential for evaluating win conditions and strategic planning.
 * 
 * Additional Considerations:
 * - Consider how the swap rule affects the initialization and application of
 * moves, especially in the context of the first few moves of the game.
 * - Efficiently updating and querying the state of the board and its components
 * is crucial, particularly for performance in evaluating win conditions and
 * generating valid moves.
 */
public class BeehiveBoard implements Board {
    public static class BeeHiveMove implements Move, java.io.Serializable {
        // Implementation of BeeHiveMove
        int row;
        int col;
        public boolean SWAP;
        public int previousOwner;

        public BeeHiveMove() {
            this.row = -1;
            this.col = -1;
            this.SWAP = false;
        }

        public BeeHiveMove(BeeHiveMove move) {
            this.row = move.row;
            this.col = move.col;
            this.SWAP = move.SWAP;
            previousOwner = move.previousOwner;
        }

        public BeeHiveMove(int row, int col) {
            this.row = row;
            this.col = col;
            this.SWAP = false;
        }

        public BeeHiveMove(boolean swap) {
            this.row = -1;
            this.col = -1;
            this.SWAP = swap;
        }

        @Override
        public int compareTo(Board.Move o) {
            BeeHiveMove otherMove = (BeeHiveMove) o;
            if (this.row == otherMove.row) {
                return Integer.compare(this.col, otherMove.col);
            } else {
                return Integer.compare(this.row, otherMove.row);
            }
        }

        @Override
        public void write(OutputStream os) throws IOException {
            if (SWAP)
                os.write(0);
            else
                os.write(((row + 1) << 4) | (col + 1));

        }

        @Override
        public void read(InputStream is) throws IOException {
            // Implementation of read method
            int b = is.read();
            if (b == -1) {
                throw new IOException("End of stream");
            }
            if (b == 0) {
                this.SWAP = true;
            } else {
                row = (b >> 4) - 1;
                col = (b & 0x0F) - 1;
            }
        }

        @Override
        public void fromString(String s) throws IOException {
            // Implementation of fromString method
            if (s.equals("SWAP")) {
                this.SWAP = true;
                return;
            } else {
                String[] parts = s.split(",");
                if (parts.length != 2) {
                    throw new IOException("Invalid move string");
                }
                try {
                    row = Integer.parseInt(parts[0]);
                    col = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid move string");
                }
            }
        }

        @Override
        public String toString() {
            return (row) + "," + (col);
        }
    }

    private Cell[][] board;
    private int currentPlayer = 1;
    private List<BeeHiveMove> moveHistory = new ArrayList<>();
    private List<Connections> connections = new ArrayList<>();
    private List<BridgeGroup> bridgeGroups = new ArrayList<>();
    private boolean hasSwapped = false;
    private static final int SIZE = 11;
    private BridgeGroup longestGroup = null;
    private boolean isGameOver = false;
    private int totalPlayer1 = 0;
    private int totalPlayer2 = 0;

    public BeehiveBoard() {
        // Initialize the board with empty cells
        board = new Cell[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Cell currentCell = new Cell(row, col);
                currentCell.computeAdjacentLocations();
                currentCell.computeBridgedLocations();
                board[row][col] = currentCell;
            }
        }
    }

    @Override
    public Move createMove() {
        return new BeeHiveMove();
    }

    @Override
    public void applyMove(Move m) throws Board.InvalidMoveException {
        try {

            // check of it is a swap move if it is a swap move then check if it is the first
            // move
            // and the current player is player 2 and the swap has not been done before
            // if all the conditions are met then perform the swap move
            if (moveHistory.size() == 1 && currentPlayer == 2 && !hasSwapped) {
                BeeHiveMove move = new BeeHiveMove(BeeHiveMove.class.cast(m));
                if (move.SWAP || (move.row == -1 && move.col == -1)) {
                    // Swap the current player
                    currentPlayer = 3 - currentPlayer;
                    // Swap the owner of the piece at the last move position
                    BeeHiveMove lastMove = moveHistory.get(moveHistory.size() - 1);
                    if (lastMove.row >= 0 && lastMove.row < board.length && lastMove.col >= 0
                            && lastMove.col < board[0].length) {
                        board[lastMove.row - 1][lastMove.col - 1].setOwner(currentPlayer);
                    }
                    hasSwapped = true;
                    moveHistory.add(move);
                    recalculateBridgeGroups();
                    return;
                }
            }

            // Check if the game is already over
            if (isGameOver) {
                throw new Board.InvalidMoveException("Game is over");
            }

            // Cast and validate the move
            BeeHiveMove move = new BeeHiveMove(BeeHiveMove.class.cast(m));
            if (move.row < 1 || move.row > 11 || move.col < 1 || move.col > 11 && !(move.row == -1 && move.col == -1)) {
                throw new Board.InvalidMoveException("Move out of bounds" + move.row + " " + move.col);
            }

            // Clear bridge groups before applying new move
            bridgeGroups.clear();

            // Handle swap move
            if (move.SWAP && moveHistory.size() == 1 && !hasSwapped && currentPlayer == 2) {
                // Perform the swap
                BeeHiveMove firstMove = moveHistory.get(0);
                board[firstMove.row - 1][firstMove.col - 1].setOwner(currentPlayer);
                hasSwapped = true;
                currentPlayer = 3 - currentPlayer;
            }
            // Handle regular move
            else {
                // Check if the target cell is already occupied
                if (board[move.row - 1][move.col - 1].player != 0) {
                    throw new Board.InvalidMoveException("Invalid move");
                }
                // Apply the move
                board[move.row - 1][move.col - 1].setOwner(currentPlayer);

                // Remove any connections that are no longer valid due to this move
                Connections connectionToRemove = null;
                for (Connections conn : connections) {
                    if ((conn.cell1.getRow() == move.row - 1 && conn.cell1.getCol() == move.col - 1) ||
                            (conn.cell2.getRow() == move.row - 1 && conn.cell2.getCol() == move.col - 1)) {
                        connectionToRemove = conn;
                        break;
                    }
                }
                if (connectionToRemove != null) {
                    connections.remove(connectionToRemove);
                }

                // Switch to the next player
                currentPlayer = 3 - currentPlayer;
            }

            // After handling the move, add it to move history and reevaluate bridge groups
            moveHistory.add(move);
            recalculateBridgeGroups();

            // Check if the game is won
            int gameState = getValue();
            if (gameState == WIN || gameState == -WIN) {
                isGameOver = true;
            }
        } catch (Board.InvalidMoveException e) {
            System.out.println(e.getMessage());
            System.out.println("Possible moves: " + getValidMoves());
            throw e; // Optionally rethrow the exception if you want to signal an invalid move beyond
                     // this method
        }
    }

    private void recalculateBridgeGroups() {
        // First, reset the 'visited' and 'group' properties for all cells on the board.
        for (Cell[] row : board) {
            for (Cell c : row) {
                c.visited = false;
                c.group = null;
            }
        }

        // Clear the list of existing bridge groups before recalculating.
        bridgeGroups.clear();

        // Iterate over each cell in the board to identify and form new bridge groups.
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Cell currentCell = board[row][col];
                // Check if the cell has not been visited and belongs to a player.
                if (!currentCell.visited && currentCell.player != 0) {
                    BridgeGroup newGroup = new BridgeGroup();
                    // Perform a depth-first search starting from the current cell to find
                    // all connected cells that form a bridge group.
                    BFS(currentCell, newGroup, currentCell.player);
                    // Add the newly formed bridge group to the list of bridge groups.
                    bridgeGroups.add(newGroup);
                }
            }
        }
    }

    @Override
    // Initially, treat each cell as an independent group. Through Depth-First
    // Search (DFS),
    // explore each cell to identify and assemble groups of connected cells, where
    // connectivity
    // is determined by adjacency and bridging, and each cell belongs to the same
    // player. A cell,
    // once visited, should not be reassigned to another group. Connectivity is
    // established either
    // through direct adjacency or via bridging, following specific game rules. This
    // process utilizes
    // supporting methods and classes (e.g., Cell, Connection, BridgeGroup, and
    // Location) for implementation
    // details.
    // The game's outcome is determined by the 'isWinning' method, which evaluates
    // if a player has successfully
    // connected their cells across the board either from top to bottom or from left
    // to right. The game is
    // designed to have a definitive winner, hence it does not support a draw; the
    // 'isWinning' method is
    // pivotal in determining the game's winner based on the established connection
    // criteria.
    public int getValue() {
        longestGroup = null;
        totalPlayer1 = 0;
        totalPlayer2 = 0;
        bridgeGroups.clear();

        // Mark all cells as unvisited in preparation for group calculation.
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col].visited = false;
            }
        }

        // Identify and form bridge groups for cells that belong to a player and have
        // not been visited.
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!board[row][col].visited && board[row][col].player != 0) {
                    BridgeGroup newGroup = new BridgeGroup();
                    BFS(board[row][col], newGroup, board[row][col].player);
                    bridgeGroups.add(newGroup);

                    // Accumulate the total weight for each player based on their bridge groups.
                    if (board[row][col].player == 1) {
                        totalPlayer1 += newGroup.getWeight(1);
                    } else {
                        totalPlayer2 += newGroup.getWeight(2);
                    }
                }
            }
        }

        // Evaluate if any bridge group meets the winning criteria, indicating game
        // completion.
        for (BridgeGroup group : bridgeGroups) {
            if (Winning(group)) {
                longestGroup = group;
                return group.getOwner() == 1 ? WIN : -WIN; // Return the win status based on the group's owner.
            }
        }

        // If no winning group is found, calculate and return the difference in total
        // weights as the game state.
        // This implies the game continues, with the higher weight indicating the
        // leading player.
        return totalPlayer1 - totalPlayer2;
    }

    /**
     * Checks if a given cell is part of any bridge group, indicating strategic
     * importance.
     * This is a simplified stand-in for potentially more complex
     * bridge-participation checks.
     * 
     * @param cell The cell to check.
     * @return True if the cell is part of a bridge, false otherwise.
     */
    private void BFS(Cell cell, BridgeGroup Bgroup, int player) {
        // Skip if the cell has already been visited
        if (cell.visited)
            return;

        cell.visited = true; // Mark the cell as visited

        // Add the cell to the group if it belongs to the current player
        if (cell.player == player) {
            Bgroup.add(cell);
        }

        // Explore all adjacent cells
        for (Location adjacentLocation : cell.adjacent) {
            Cell adjacentCell = board[adjacentLocation.row][adjacentLocation.col];
            // Recursively explore if the adjacent cell is unvisited and belongs to the same
            // player
            if (!adjacentCell.visited && adjacentCell.player == player) {
                BFS(adjacentCell, Bgroup, player);
            }
        }

        // Explore all cells connected by bridges
        for (Location bridgedLocation : cell.bridged) {
            Cell bridgedCell = board[bridgedLocation.row][bridgedLocation.col];
            // Check for valid bridges and if the bridged cell can be part of the group
            if (!bridgedCell.visited && bridgedCell.player == player && isValidBridge(cell, bridgedCell)) {
                BFS(bridgedCell, Bgroup, player);
            }
        }
    }

    public boolean isValidBridge(Cell firstCell, Cell secondCell) {
        // Check if the bridge between two cells is valid
        // Return true if the bridge is valid, false otherwise

        // Add more conditions to check if the bridge is valid
        // For example, check if the cells are adjacent and empty
        // or if the cells are connected by a chain of adjacent cells

        // If none of the conditions are met, the bridge is not valid
        if (firstCell.player != secondCell.player) {
            return false;
        }

        List<Location> intermediateLocations = commonLocations(firstCell, secondCell);
        for (Location location : intermediateLocations) {
            if (board[location.getRow()][location.getCol()].player != 0) {
                return false;
            }
        }

        return true;

    }

    private List<Location> commonLocations(Cell firstCell, Cell secondCell) {
        // Recalculate adjacent locations for both cells to ensure up-to-date
        // information
        firstCell.computeAdjacentLocations();
        secondCell.computeAdjacentLocations();

        // Prepare lists to hold adjusted adjacent locations for comparison
        List<Location> adjustedLocationsCellOne = new ArrayList<>();
        List<Location> adjustedLocationsCellTwo = new ArrayList<>();

        // Adjust and collect adjacent locations for the first cell
        for (Location location : firstCell.adjacent) {
            if (location != null) {
                Location adjustedLocation = new Location(location.row + 1, location.col + 1);
                adjustedLocationsCellOne.add(adjustedLocation);
            }
        }

        // Adjust and collect adjacent locations for the second cell
        for (Location location : secondCell.adjacent) {
            if (location != null) {
                Location adjustedLocation = new Location(location.row + 1, location.col + 1);
                adjustedLocationsCellTwo.add(adjustedLocation);
            }
        }

        // Determine the common locations between the two cells' adjacent locations
        List<Location> commonLocations = new ArrayList<>();
        for (Location location : adjustedLocationsCellOne) {
            if (adjustedLocationsCellTwo.contains(location)) {
                commonLocations.add(location);
            }
        }

        return commonLocations;
    }

    @Override
    public List<? extends Move> getValidMoves() {
        // Generate and return a list of all legal moves for the current player
        // based on the current state of the board and the game's rules
        List<BeeHiveMove> validMoves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j].player == 0) {
                    validMoves.add(new BeeHiveMove(i + 1, j + 1));
                }
            }
        }
        if (moveHistory.size() == 1 && !hasSwapped && currentPlayer == 2) {
            validMoves.add(new BeeHiveMove(true));
        }
        return validMoves;
    }

    @Override
    public int getCurrentPlayer() {
        // Return the identifier for the current player
        return currentPlayer;
    }

    @Override
    public List<? extends Move> getMoveHistory() {
        // Return the move history
        return moveHistory;
    }

    @Override
    public void undoMove() {
        // Undo the last move made
        if (moveHistory.isEmpty()) {
            return;
        }

        // Get the last move from the move history
        BeeHiveMove lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Handle swap move
        if (lastMove.SWAP) {
            currentPlayer = 3 - currentPlayer;
            hasSwapped = false;
        }
        // Handle regular move
        else {
            // Clear bridge groups before undoing the move
            bridgeGroups.clear();

            // Undo the move by resetting the cell state
            board[lastMove.row - 1][lastMove.col - 1].setOwner(0);

            // Switch back to the previous player
            currentPlayer = 3 - currentPlayer;
        }

        // Recalculate bridge groups after undoing the move
        recalculateBridgeGroups();

        // Reset the game state if the game was over
        if (isGameOver) {
            isGameOver = false;
        }
    }

    private boolean spansDirection(BridgeGroup Bgroup, String direction) {
        List<Cell> edgeCells;
        if ("leftToRight".equals(direction)) {
            edgeCells = Bgroup.cells.stream().filter(cell -> cell.getCol() == 0).collect(Collectors.toList());
        } else if ("topToBottom".equals(direction)) {
            edgeCells = Bgroup.cells.stream().filter(cell -> cell.getRow() == 0).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid direction");
        }

        Bgroup.cells.forEach(c -> c.visited = false); // Reset visited status for all cells in the group

        Queue<Cell> queue = new LinkedList<>();
        // Enqueue all edge cells as starting points for the BFS
        edgeCells.forEach(startCell -> {
            if (!startCell.visited) {
                queue.add(startCell);
                startCell.visited = true;
            }
        });

        while (!queue.isEmpty()) {
            Cell currentCell = queue.poll();
            // Check for reaching the opposite edge based on the direction
            if (("leftToRight".equals(direction) && currentCell.getCol() == SIZE - 1) ||
                    ("topToBottom".equals(direction) && currentCell.getRow() == SIZE - 1)) {
                return true;
            }

            // Enqueue all adjacent, unvisited cells belonging to the same player
            for (Location loc : currentCell.adjacent) {
                Cell adjCell = board[loc.row][loc.col];
                if (!adjCell.visited && adjCell.player == currentCell.player) {
                    queue.add(adjCell);
                    adjCell.visited = true; // Mark as visited once added to the queue
                }
            }
        }

        return false; // If queue empties without reaching the target edge, it does not span in the
                      // desired direction
    }

    private boolean Winning(BridgeGroup Bgroup) {
        // Check if the bridge group spans from left to right or top to bottom
        if (Bgroup == null || Bgroup.cells.isEmpty()) {
            return false;
        }
        return spansDirection(Bgroup, "leftToRight") || spansDirection(Bgroup, "topToBottom");
    }

    @Override
    // Return a string representation of the board
    // Useful for debugging or displaying the game in a text-based interface
    // Return a string representation of the board, with " b" for player 1's stones,
    // " r" for player 2's stones, and " ." for empty cells. Offset each row right
    // by 1/2 column to make the board look trapezoidal.
    // Show row and column numbers on the left and top, using "T" and "E" for 10 and
    // 11. If the game is over, show the cells in the winning group in upper case.

    // Example:
    // 1 2 3 4 5 6 7 8 9 T E
    // 1 . . . . . . . . . . .
    // 2 . . . . . . . . . . .
    // 3 . . . . r . . . b . b
    // 4 . . . . . . b . . . .
    // 5 . . . r b . . b . b .
    // 6 . . . r r . . . . . .
    // 7 . . . . . . . . . . .
    // 8 . . . r . . . . . . .
    // 9 . . . . . . . . . . .
    // T . . r . . . . . . . .
    // E . . . . . . . . . . .
    // Player 2's move
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Add column headers
        sb.append("  1 2 3 4 5 6 7 8 9 T E\n");

        for (int row = 0; row < SIZE; row++) {
            // Add row indentation for hexagonal appearance
            for (int indent = 0; indent < row; indent++) {
                sb.append(" ");
            }
            // Add row header with 'T' and 'E' for 10 and 11
            sb.append(row < 9 ? row + 1 : row == 9 ? "T" : "E").append(" ");

            for (int col = 0; col < SIZE; col++) {
                Cell cell = board[row][col];
                char cellRepresentation = '.'; // Default for empty cell

                // Check if the cell is part of the winning group, if the game is over
                boolean isInWinningGroup = isGameOver && longestGroup != null && longestGroup.cells.contains(cell);

                if (cell.player == 1) {
                    cellRepresentation = isInWinningGroup ? 'B' : 'b'; // Uppercase 'B' for winning path, else 'b'
                } else if (cell.player == 2) {
                    cellRepresentation = isInWinningGroup ? 'R' : 'r'; // Uppercase 'R' for winning path, else 'r'
                }
                // If players have swapped, swap the cell representation
                if (hasSwapped) {
                    if (cellRepresentation == 'b') {
                        cellRepresentation = 'r';
                    } else if (cellRepresentation == 'r') {
                        cellRepresentation = 'b';
                    } else if (cellRepresentation == 'B') {
                        cellRepresentation = 'R';
                    } else if (cellRepresentation == 'R') {
                        cellRepresentation = 'B';
                    }
                }

                sb.append(cellRepresentation).append(" "); // Add cell representation and space for next cell
            }
            sb.append("\n"); // New line at the end of each row
        }

        // Optional: Append additional game state information
        sb.append("Player ").append(currentPlayer == 1 ? "1" : "2").append("'s turn.\n");
        if (isGameOver) {
            sb.append("Player ").append(longestGroup.getOwner() == 1 ? "1 has won." : "2 has won.").append("\n");
        }
        sb.append("\nBridge Groups:\n");
        for (int i = 0; i < bridgeGroups.size(); i++) {
            BridgeGroup group = bridgeGroups.get(i);
            sb.append("Group ").append(i + 1).append(": ");
            sb.append("Owner = ").append(group.getOwner() == 1 ? "1" : "2").append(", ");
            sb.append("Size = ").append(group.cells.size()).append("\n");
        }

        return sb.toString();

    }
}