package BoardGames.BeehiveBoard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import BoardGames.Board;
import BoardGames.Board.InvalidMoveException;
import BoardGames.Board.Move;

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

        public BeeHiveMove() {
            this.row = -1;
            this.col = -1;
        }

        public BeeHiveMove(int row2, int col2) {
            this.row = row2;
            this.col = col2;
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
            os.write(((row + 1) << 4) | (col + 1));
        }

        @Override
        public void read(InputStream is) throws IOException {
            // Implementation of read method
            int b = is.read();
            row = (b >> 4) - 1;
            col = (b & 0x0F) - 1;
        }

        @Override
        public void fromString(String s) throws IOException {
            // Implementation of fromString method
            String[] parts = s.split(",");
            if (parts.length != 2) {
                throw new IOException("Invalid move string");
            }
            try {
                row = Integer.parseInt(parts[0]) - 1;
                col = Integer.parseInt(parts[1]) - 1;
                if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE)
                    throw new IOException("Invalid move string");
            } catch (NumberFormatException e) {
                throw new IOException("Invalid move string");
            }
        }

        public boolean isSwap() {
            return this.row == -1 && this.col == -1;
        }

        @Override
        public String toString() {
            return (row + 1) + "," + (col + 1);
        }
    }

    private static final int BOARD_SIZE = 11;
    private Cell[][] board = new Cell[BOARD_SIZE][BOARD_SIZE];
    private int currentPlayer;
    private List<Move> moveHistory = new ArrayList<>();
    private Connections connections;
    private int currentWinner;
    private List<BridgeGroup> groups;

    public BeehiveBoard() {
        // Initialize the board and set the starting player
        Connections connections = new Connections();
        groups = new ArrayList<>();
        this.connections = new Connections();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
        currentPlayer = 1; // or 0, depending on how you define players
    }

    @Override
    public Move createMove() {
        return new BeeHiveMove();
    }

    @Override
    public void applyMove(Move m) throws Board.InvalidMoveException {
        BeeHiveMove move = (BeeHiveMove) m;
        if (!isValidMove(move)) {
            throw new Board.InvalidMoveException("Invalid move");
        }
        if (move.isSwap()) {
            currentPlayer = -currentPlayer;
        } else {
            board[move.row][move.col].setState(currentPlayer);
            Location location = new Location(move.row, move.col);
            updateGroups(location, currentPlayer);
            currentPlayer = -currentPlayer;
        }
        moveHistory.add(move);
    }

    private void updateGroups(Location location, int player) {
        List<BridgeGroup> adjacentGroups = new ArrayList<>();
        for (BridgeGroup group : groups) {
            if (group.getPlayer() == player) {
                for (Location cell : group.getCells()) {
                    if (connections.isAdjacent(location, cell) || connections.isBridge(location, cell)) {
                        adjacentGroups.add(group);
                        break;
                    }
                }
            }
        }

        if (adjacentGroups.isEmpty()) {
            // No existing group found; create a new group for this move
            BridgeGroup newGroup = new BridgeGroup(this);
            newGroup.add(location);
            newGroup.setPlayer(player);
            groups.add(newGroup);
        } else {
            // Add this cell to the first adjacent group
            BridgeGroup firstGroup = adjacentGroups.get(0);
            firstGroup.add(location);

            // Merge all adjacent groups into the first group
            for (int i = 1; i < adjacentGroups.size(); i++) {
                BridgeGroup group = adjacentGroups.get(i);
                firstGroup.merge(group);
                groups.remove(group);
            }
        }
    }

    private boolean isAdjacentOrBridged(Location location1, Location location2) {
        int rowDiff = Math.abs(location1.getRow() - location2.getRow());
        int colDiff = Math.abs(location1.getCol() - location2.getCol());
        return (rowDiff <= 1 && colDiff <= 1) || (rowDiff <= 2 && colDiff <= 2);
    }

    @Override
    public int getValue() {
        // Check for win conditions and return the appropriate value
        // Return an integer indicating whether player 0 (negative value)
        // or player 1 (positive value) is winning, or 0 if the game is a draw
        // (0 value). A value of WIN indicates a win for player 0, and -WIN
        // indicates a win for player 1
        // Check for win conditions based on the connectivity of cells across the board
        // Return WIN if player 0 wins, -WIN if player 1 wins, or 0 if the game is a
        // draw
        // Use the BridgeGroup objects to determine if a player has successfully formed
        // a
        // connecting path across the board

        // Initialize the value to 0
        // int player1Score = 0;
        // int player2Score = 0;

        // // Iterate over all cells in the board
        // for (int row = 0; row < BOARD_SIZE; row++) {
        // for (int col = 0; col < BOARD_SIZE; col++) {
        // // Get the state of the current cell
        // int state = board[row][col].getState();

        // // If the cell is occupied by a player, check for connections
        // if (state != 0) {
        // // Create a new Location for the current cell
        // Location location = new Location(row, col);

        // // Check for connections from this location to another cell
        // for (Location other : connections.getAdjacentLocations(location)) {
        // if (board[other.getRow()][other.getCol()].getState() == state) {
        // if (state == 1) {
        // player1Score++;
        // } else if (state == -1) {
        // player2Score++;
        // }
        // }
        // }
        // }
        // }
        // }

        // // Determine the winner based on the scores
        // currentWinner = player1Score > player2Score ? 1 : -1;
        // return player1Score > player2Score ? player1Score : -player2Score;

        // Assess the current state of the board to calculate a score or advantage.
        // Positive values indicate an advantage for Player 1, negative for Player 2,
        // and zero for an even state.

        int player1Score = 0;
        int player2Score = 0;

        // Evaluate each cell on the board for its contribution to the player's score
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = board[row][col];
                if (cell.getState() == 1) { // Player 1 occupies the cell
                    player1Score += 1; // Assign a base score for occupation
                    // Further evaluate the cell's strategic importance
                    // For example, proximity to forming a bridge might increase its value
                    if (isPartOfBridge(cell)) {
                        player1Score += 2; // Increment score for strategic positioning
                    }
                } else if (cell.getState() == -1) { // Player 2 occupies the cell
                    player2Score += 1; // Assign a base score for occupation
                    // Evaluate the cell's strategic importance
                    if (isPartOfBridge(cell)) {
                        player2Score += 2; // Increment score for strategic positioning
                    }
                }
            }
        }

        // Evaluate the impact of bridge groups
        for (BridgeGroup group : groups) {
            if (group.getPlayer() == 1) {
                // For Player 1, increase the score based on the group size or strategic value
                player1Score += group.getCells().length; // Example: score increases with group size
            } else if (group.getPlayer() == -1) {
                // For Player 2, similarly increase the score
                player2Score += group.getCells().length;
            }
        }

        // Return the net score: positive for Player 1's advantage, negative for Player
        // 2's
        return player1Score - player2Score;
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
    private boolean isPartOfBridge(Cell cell) {
        for (BridgeGroup group : groups) {
            if (Arrays.asList(group.getCells()).contains(cell.getLocation())) {
                return true; // The cell is part of a bridge group
            }
        }
        return false; // The cell is not part of any bridge group
    }

    public int getCurrentWinner() {
        return currentWinner;
    }

    @Override
    public List<? extends Move> getValidMoves() {
        // Generate a list of all valid moves
        // Return a list of all valid moves for the current player. An empty list
        // indicates that the game is over.

        List<BeeHiveMove> validMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isEmpty()) {
                    validMoves.add(new BeeHiveMove(i, j));
                }
            }
        }
        return validMoves;

    }

    @Override
    public int getCurrentPlayer() {
        // Return the current player
        // Return 1 if player 0 is to move, -1 if player 1 is to move
        return currentPlayer;
    }

    @Override
    public List<? extends Move> getMoveHistory() {
        // Return the move history
        // Return a history of all moves thus far applied to the board
        return moveHistory;
    }

    @Override
    public void undoMove() {
        // Reverse the last move made
        // Undo most recent move, or do nothing if no moves have been made

        if (!moveHistory.isEmpty()) {
            BeeHiveMove lastMove = (BeeHiveMove) moveHistory.remove(moveHistory.size() - 1);
            if (!lastMove.isSwap()) {
                board[lastMove.row][lastMove.col].setState(0);
            } else {
                currentPlayer = -currentPlayer;
            }
        }
    }

    @Override
    public String toString() {
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
        BridgeGroup longestGroup = null;
        for (BridgeGroup group : groups) {
            if (longestGroup == null || group.size() > longestGroup.size()) {
                longestGroup = group;
            }
        }

        StringBuilder sb = new StringBuilder();
        String markers = " 1 2 3 4 5 6 7 8 9 T E\n";
        sb.append(markers);
        List<Cell> winningPath = null;
        if (hasWon(1)) {
            winningPath = dfs(0, 0, new boolean[BOARD_SIZE][BOARD_SIZE], 1);
        } else if (hasWon(-1)) {
            winningPath = dfs(0, 0, new boolean[BOARD_SIZE][BOARD_SIZE], -1);
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < i; j++) {
                sb.append(" ");
            }
            sb.append(markers.charAt(i * 2));
            sb.append(markers.charAt(i * 2 + 1));
            for (int j = 0; j < BOARD_SIZE; j++) {
                sb.append(" ");
                int state = board[i][j].getState();
                boolean isWinningCell = winningPath != null && winningPath.contains(board[i][j]);
                Location location = new Location(i, j);
                if (state == 1) {
                    sb.append((isWinningCell || (longestGroup != null && longestGroup.contains(location))) ? "B" : "b");
                } else if (state == -1) {
                    sb.append((isWinningCell || (longestGroup != null && longestGroup.contains(location))) ? "R" : "r");
                } else {
                    sb.append(".");
                }
            }
            sb.append("\n");
        }
        sb.append("Player " + (currentPlayer == 1 ? "1" : "2") + "'s move\n");
        if (hasWon(1)) {
            sb.append("Player 1 has won the game\n");
        } else if (hasWon(-1)) {
            sb.append("Player 2 has won the game\n");
        }

        for (BridgeGroup group : groups) {
            // Print the group details
            System.out.println("Group: " + group.toString());

            // Print the cells in the group
            for (Location cell : group.getCells()) {
                System.out.println("  Cell: " + cell.toString());
            }
        }
        return sb.toString();

        // StringBuilder sb = new StringBuilder();
        // String markers = " 1 2 3 4 5 6 7 8 9 T E\n";
        // sb.append(markers);
        // List<Cell> winningPath = null;
        // if (hasWon(1)) {
        // winningPath = dfs(0, 0, new boolean[BOARD_SIZE][BOARD_SIZE], 1);
        // } else if (hasWon(-1)) {
        // winningPath = dfs(0, 0, new boolean[BOARD_SIZE][BOARD_SIZE], -1);
        // }
        // for (int i = 0; i < BOARD_SIZE; i++) {
        // for (int j = 0; j < i; j++) {
        // sb.append(" ");
        // }
        // sb.append(markers.charAt(i * 2));
        // sb.append(markers.charAt(i * 2 + 1));
        // for (int j = 0; j < BOARD_SIZE; j++) {
        // sb.append(" ");
        // int state = board[i][j].getState();
        // boolean isWinningCell = winningPath != null &&
        // winningPath.contains(board[i][j]);
        // if (state == 1) {
        // sb.append(isWinningCell ? "B" : "b");
        // } else if (state == -1) {
        // sb.append(isWinningCell ? "R" : "r");
        // } else {
        // sb.append(".");
        // }
        // }
        // sb.append("\n");
        // }
        // sb.append("Player " + (currentPlayer == 1 ? "1" : "2") + "'s move\n");
        // return sb.toString();
    }

    private boolean hasWon(int i) {
        // checks who has won based on the get value function
        // result is used in the toString function to display the winning player in
        // uppercase
        return getValue() == i * WIN;
    }

    private boolean isValidMove(BeeHiveMove move) {
        // Check if the move is valid based on game rules
        // Return true if the move is valid, false otherwise

        if (move.isSwap()) {
            return true;
        }

        // Add more conditions to check if the move is valid
        // For example, check if the cell at the move's row and column is empty
        if (board[move.row][move.col].isEmpty()) {
            return true;
        }

        // If none of the conditions are met, the move is not valid
        return false;
    }

    public Connections getConnections() {

        return connections;
    }

    private boolean isConnected(int player) {
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        // For player 1, start from the top row
        if (player == 1) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[0][col].getState() == player) {
                    if (dfs(0, col, visited, player) != null) {
                        return true;
                    }
                }
            }
        }
        // For player 2, start from the leftmost column
        else if (player == -1) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                if (board[row][0].getState() == player) {
                    if (dfs(row, 0, visited, player) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Cell> dfs(int row, int col, boolean[][] visited, int player) {
        if ((player == 1 && row == BOARD_SIZE - 1) || (player == -1 && col == BOARD_SIZE - 1)) {
            return new ArrayList<>(Arrays.asList(board[row][col]));
        }

        visited[row][col] = true;

        int[] dr = { -1, -1, 0, 0, 1, 1 };
        int[] dc = { -1, 0, -1, 1, 0, 1 };

        for (int i = 0; i < 6; i++) {
            int newRow = row + dr[i];
            int newCol = col + dc[i];
            if (newRow >= 0 && newRow < BOARD_SIZE && newCol >= 0 && newCol < BOARD_SIZE
                    && !visited[newRow][newCol] && board[newRow][newCol].getState() == player) {
                List<Cell> path = dfs(newRow, newCol, visited, player);
                if (path != null) {
                    path.add(board[row][col]);
                    return path;
                }
            }
        }

        return null;
    }

    public boolean isPlayerOneSide(Location cell) {
        return cell.getRow() == 0;
    }

    public boolean isOppositeSide(Location current, int player) {
        return player == 1 ? current.getRow() == BOARD_SIZE - 1 : current.getCol() == BOARD_SIZE - 1;
    }

    public void addGroup(BridgeGroup group) {
        groups.add(group);
    }

    public void removeGroup(BridgeGroup group) {
        groups.remove(group);
    }

    public BridgeGroup getGroup(Location cell) {
        // Iterate over all groups
        for (BridgeGroup group : groups) {
            // If the group contains the cell, return the group
            if (group.contains(cell)) {
                return group;
            }
        }

        // If no group contains the cell, return null
        return null;
    }

    public int getState(Location cell) {
        return board[cell.getRow()][cell.getCol()].getState();
    }

    public void setGroup(Location cell, BridgeGroup bridgeGroup) {
        // Set the group of the cell
        board[cell.getRow()][cell.getCol()].setGroup(bridgeGroup);
    }

}