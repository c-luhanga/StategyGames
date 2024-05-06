package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import edu.principia.OODesign.StrategyGames.Board;

public class BeehiveBoard implements Board, Serializable {
    public class BeeHiveBoardMove implements Board.Move, Serializable {
        public int row;
        public int col;
        public boolean swap;
        public int previousOwner;

        public BeeHiveBoardMove() {
            this.row = -1; // Initialize row out of valid range
            this.col = -1; // Initialize column out of valid range
            this.swap = false; // By default, not a swap move
        }

        public BeeHiveBoardMove(int row, int col) {
            this.row = row;
            this.col = col;
            this.swap = false;
        }

        public BeeHiveBoardMove(boolean swap) {
            this.row = -1; // Swap doesn't need row and column
            this.col = -1;
            this.swap = swap;
        }

        @Override
        public void write(OutputStream os) throws IOException {
            if (swap) {
                os.write(0); // Represent swap with a unique identifier, e.g., 0
            } else {
                int b = (row << 4) | col; // Example packing of row and column
                os.write(b);
            }
        }

        @Override
        public void read(InputStream is) throws IOException {
            int b = is.read();
            if (b == -1)
                throw new IOException("End of stream");

            if (b == 0) {
                this.swap = true;
            } else {
                this.swap = false;
                this.row = (b >> 4) & 0x0F; // Extract row
                this.col = b & 0x0F; // Extract column
            }
        }

        @Override
        public void fromString(String s) throws IOException {
            if (s.equals("swap")) {
                this.swap = true;
            } else {
                this.swap = false;
                String[] parts = s.split(",");
                if (parts.length != 2) {
                    throw new IOException("Invalid format");
                }
                try {
                    this.row = Integer.parseInt(parts[0].trim()) - 1;
                    this.col = Integer.parseInt(parts[1].trim()) - 1;
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid move string");
                }
            }
        }

        @Override
        public int compareTo(Move other) {
            if (!(other instanceof BeeHiveBoardMove)) {
                return 0; // Or throw an exception or make this a defined behavior
            }
            BeeHiveBoardMove o = (BeeHiveBoardMove) other;
            if (this.swap && !o.swap) {
                return -1;
            } else if (!this.swap && o.swap) {
                return 1;
            } else if (this.swap && o.swap) {
                return 0; // Both are swaps
            } else {
                // Compare by row and then by column if not swap
                if (this.row != o.row) {
                    return Integer.compare(this.row, o.row);
                } else {
                    return Integer.compare(this.col, o.col);
                }
            }
        }

        @Override
        public String toString() {
            if (swap) {
                return "swap";
            } else {
                return row + "," + col;
            }
        }
    }

    private Cell[][] board;
    private int currentPlayer;
    private List<BeeHiveBoardMove> moveHistory;
    List<Connections> connections;
    List<BridgeGroup> bridgeGroups;
    boolean hasSwapped;
    static final int SIZE = 11;
    BridgeGroup winningGroup = null;
    private boolean isGameOver = false;
    int totalWeightPlayer1 = 0;
    int totalWeightPlayer2 = 0;

    public BeehiveBoard() {
        board = new Cell[SIZE][SIZE];
        currentPlayer = PLAYER_0;
        moveHistory = new ArrayList<BeeHiveBoardMove>();
        connections = new ArrayList<Connections>();
        bridgeGroups = new ArrayList<BridgeGroup>();
        hasSwapped = false;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = new Cell(i, j);
            }
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j].computeAdj();
                board[i][j].computeBridge();
            }
        }

    }

    @Override
    public Move createMove() {
        return new BeeHiveBoardMove();
    }

    @Override
    public void applyMove(Move m) throws InvalidMoveException {
        try {
            if (isGameOver) {
                throw new Board.InvalidMoveException("Game is over");
            }
            BeeHiveBoardMove move = (BeeHiveBoardMove) m;

            System.out.println("Attempting move" + move.toString());
            System.out.println("Attempting move at: row " + move.row + ", col " + move.col);

            if (move.row < 0 || move.row >= SIZE || move.col < 0 || move.col >= SIZE) {
                throw new Board.InvalidMoveException("Move out of bounds");
            }
            // Clear previous bridge groups before recalculating
            bridgeGroups.clear();

            if (move.swap && moveHistory.size() == 1 && !hasSwapped && currentPlayer == 2) {
                move.previousOwner = board[move.row][move.col].player;
                board[move.row][move.col].setOwner(currentPlayer);
                currentPlayer = 3 - currentPlayer;
                hasSwapped = true;
            } else {
                if (board[move.row][move.col].player != 0) {
                    throw new Board.InvalidMoveException("Invalid move");
                }
                move.previousOwner = board[move.row][move.col].player;
                board[move.row][move.col].player = currentPlayer;

                Connections connectionToRemove = null;
                for (Connections conn : connections) {
                    if ((conn.cell1.row == move.row && conn.cell1.col == move.col) ||
                            (conn.cell2.row == move.row && conn.cell2.col == move.col)) {
                        connectionToRemove = conn;
                        break;
                    }
                }
                if (connectionToRemove != null) {
                    connections.remove(connectionToRemove);
                }
                currentPlayer = 3 - currentPlayer;
            }
            moveHistory.add(move);

            // Reset and recalculate bridge groups after the move
            for (Cell[] row : board) {
                for (Cell c : row) {
                    c.visited = false;
                    c.group = null; // Reset group membership
                }
            }

            // Recalculate groups after updating the board state
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (!board[row][col].visited && board[row][col].player != 0) {
                        BridgeGroup bg = new BridgeGroup();
                        dfsFindGroups(board[row][col], bg, board[row][col].player);
                        bridgeGroups.add(bg);
                    }
                }
            }

            int gameState = getValue();
            if (gameState == WIN || gameState == -WIN) {
                isGameOver = true;
            }
        } catch (Board.InvalidMoveException e) {
            System.out.println(e.getMessage());
            System.out.println("Possible moves: " + getValidMoves());
        }
    }

    public void dfsFindGroups(Cell c, BridgeGroup bg, int player) {
        if (c.visited)
            return; // Already visited this cell

        c.visited = true;
        if (c.player == player)
            bg.add(c);

        for (Location adjLoc : c.adjacent) {
            Cell adjCell = board[adjLoc.row][adjLoc.col];
            if (!adjCell.visited && adjCell.player == player) {
                dfsFindGroups(adjCell, bg, player);
            }
        }

        for (Location bridgeLoc : c.bridged) {
            Cell bridgeCell = board[bridgeLoc.row][bridgeLoc.col];
            if (!bridgeCell.visited && bridgeCell.player == player && isValidBridge(c, bridgeCell)) {
                dfsFindGroups(bridgeCell, bg, player);
            }
        }
    }

    public boolean isValidBridge(Cell c1, Cell c2) {
        if (c1.player != c2.player || c1.adjacent.contains(c2.loc))
            return false;

        List<Location> intermediateLocations = getIntermediateLocations(c1, c2);
        for (Location loc : intermediateLocations) {
            if (board[loc.row][loc.col].player != 0)
                return false;
        }
        return true;
    }

    public List<Location> getIntermediateLocations(Cell c1, Cell c2) {
        c1.computeAdj();
        c2.computeAdj();
        List<Location> intermediateLocations = new ArrayList<>();
        for (Location loc1 : c1.adjacent) {
            for (Location loc2 : c2.adjacent) {
                if (loc1.equals(loc2) && board[loc1.row][loc1.col].player == 0) {
                    intermediateLocations.add(loc1);
                }
            }
        }
        return intermediateLocations;
    }

    public void printBridgeGroupsDetailed() {
        System.out.println("Current Bridge Groups:");
        for (BridgeGroup bg : bridgeGroups) {
            System.out.println("--------------------------------");
            System.out.println("Owned by Player: " + (bg.cells.isEmpty() ? "None" : bg.cells.get(0).player));
            System.out.println("Group Span: " + bg.getSpan(bg.cells.isEmpty() ? 0 : bg.cells.get(0).player));
            System.out.println("Cells in Group:");
            for (Cell c : bg.cells) {
                System.out.println("Cell at (" + (c.row + 1) + ", " + (c.col + 1) + ")");
            }
        }
        System.out.println("--------------------------------");
    }

    @Override
    public int getValue() {
        // Initialize all cells as their own group and mark them as not visited
        winningGroup = null;
        totalWeightPlayer1 = 0;
        totalWeightPlayer2 = 0;
        bridgeGroups.clear();

        // Reset all cells to unvisited
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col].visited = false;
            }
        }

        // Find all bridge groups
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!board[row][col].visited && board[row][col].player != 0) {
                    BridgeGroup bg = new BridgeGroup();
                    dfsFindGroups(board[row][col], bg, board[row][col].player);
                    bridgeGroups.add(bg);

                    if (board[row][col].player == 1) {
                        totalWeightPlayer1 += bg.getWeight(1);
                    } else {
                        totalWeightPlayer2 += bg.getWeight(2);
                    }
                }
            }
        }

        // Check if a player has won
        for (BridgeGroup bg : bridgeGroups) {
            if (isWinning(bg)) {
                winningGroup = bg;
                return bg.getOwner() == 1 ? WIN : -WIN;
            }
        }

        // Return the difference between the total weights of the groups if no one has
        // won
        return totalWeightPlayer1 - totalWeightPlayer2;
    }

    private boolean isWinning(BridgeGroup bg) {
        if (bg == null || bg.cells.isEmpty()) {
            return false; // No cells in the group means it cannot be a winning group.
        }
        // Check winning condition based on the player.
        if (bg.cells.get(0).player == 1) {
            return spansLeftToRight(bg); // Player 1 might win by spanning left to right.
        } else {
            return spansTopToBottom(bg); // Player 2 might win by spanning top to bottom.
        }
    }

    private boolean spansLeftToRight(BridgeGroup bg) {
        List<Cell> leftEdgeCells = bg.cells.stream().filter(c -> c.col == 0).collect(Collectors.toList());
        bg.cells.forEach(c -> c.visited = false); // Reset visited for traversal.

        for (Cell startCell : leftEdgeCells) {
            if (dfsLeftToRight(startCell, bg)) {
                return true; // If DFS finds a path to the right edge, win condition is met.
            }
        }
        return false; // No path was found spanning left to right.
    }

    private boolean spansTopToBottom(BridgeGroup bg) {
        List<Cell> topEdgeCells = bg.cells.stream().filter(c -> c.row == 0).collect(Collectors.toList());
        bg.cells.forEach(c -> c.visited = false); // Reset visited for new traversal.

        for (Cell startCell : topEdgeCells) {
            if (dfsTopToBottom(startCell, bg)) {
                return true; // If DFS finds a path to the bottom edge, win condition is met.
            }
        }
        return false; // No path spanning top to bottom.
    }

    private boolean dfsLeftToRight(Cell c, BridgeGroup bg) {
        if (c.col == 10) { // 10 is the last column index for an 11x11 board.
            return true; // Reached the rightmost edge, winning condition met.
        }
        c.visited = true;
        for (Location loc : c.adjacent) {
            Cell adjCell = board[loc.row][loc.col];
            if (adjCell.player == c.player && !adjCell.visited) {
                if (dfsLeftToRight(adjCell, bg)) {
                    return true; // Recursively continue to search right.
                }
            }
        }
        return false; // No path found to the right edge.
    }

    private boolean dfsTopToBottom(Cell c, BridgeGroup bg) {
        if (c.row == 10) { // 10 is the last row index for an 11x11 board.
            return true; // Reached the bottom edge, winning condition met.
        }
        c.visited = true;
        for (Location loc : c.adjacent) {
            Cell adjCell = board[loc.row][loc.col];
            if (adjCell.player == c.player && !adjCell.visited) {
                if (dfsTopToBottom(adjCell, bg)) {
                    return true; // Continue search downwards.
                }
            }
        }
        return false; // No path found to the bottom edge.
    }

    public void recalculateBridgeGroups() {
        // Clear existing groups. This might involve resetting group-related properties
        // in each cell.
        for (Cell[] row : board) {
            for (Cell c : row) {
                c.visited = false;
                c.group = null; // Reset group membership
            }
        }
        bridgeGroups.clear(); // Clear existing groups

        // Recalculate groups. This will also update the group property in each cell.
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!board[row][col].visited && board[row][col].player != 0) {
                    BridgeGroup bg = new BridgeGroup();
                    dfsFindGroups(board[row][col], bg, board[row][col].player);
                    bridgeGroups.add(bg);
                }
            }
        }
    }

    @Override
    public List<? extends Move> getValidMoves() {
        List<BeeHiveBoardMove> validMoves = new ArrayList<>();

        // Check each cell on the board; if it's empty, add a possible move to that
        // position.
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c].player == 0) { // 0 indicates the cell is empty.
                    validMoves.add(new BeeHiveBoardMove(r + 1, c + 1)); // Cells are 1-indexed in moves.
                }
            }
        }

        // Add a swap move if only one move has been made so far and no swap has
        // occurred yet.
        if (moveHistory.size() == 1 && !hasSwapped) {
            validMoves.add(new BeeHiveBoardMove(true)); // Add the swap option.
        }

        // If the game has ended (someone has won), clear the list of valid moves.
        int gameState = getValue();
        if (gameState == WIN || gameState == -WIN) {
            validMoves.clear();
        }

        return validMoves;
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public List<? extends Move> getMoveHistory() {
        return moveHistory;
    }

    @Override
    public void undoMove() {
        if (moveHistory.isEmpty()) {
            return; // No moves to undo.
        }

        BeeHiveBoardMove lastMove = moveHistory.remove(moveHistory.size() - 1);
        BeeHiveBoardMove move = new BeeHiveBoardMove();

        if (lastMove.swap) {
            hasSwapped = false; // Reset swap flag since we're undoing the swap.

            // Assuming the swap action exchanges the ownership of the first move,
            // and that this move is always at the beginning of the moveHistory list.
            // You need to revert this change.
            if (!moveHistory.isEmpty()) {
                // Revert the ownership of the first move's cell to the original player (Player
                // 1).
                BeeHiveBoardMove firstMove = moveHistory.get(0);
                if (firstMove.row >= 1 && firstMove.row <= 11 && firstMove.col >= 1 && firstMove.col <= 11) {
                    board[firstMove.row - 1][firstMove.col - 1].player = 1; // Set back to Player 1.
                }
            }
        } else {
            // Regular move - clear the cell.
            if (move.row >= 1 && move.row <= 11 && move.col >= 1 && move.col <= 11) {
                board[move.row - 1][move.col - 1].player = 0;
            }
        }

        currentPlayer = 3 - currentPlayer; // Adjust the current player accordingly.

        recalculateBridgeGroups(); // Recalculate bridge groups after the move is undone.

        // Reassess the game state to update gameOver status.
        isGameOver = false;
        int gameState = getValue();
        if (gameState == WIN || gameState == -WIN) {
            isGameOver = true;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("  1 2 3 4 5 6 7 8 9 T E\n");
        for (int r = 0; r < 11; r++) {
            for (int i = 0; i < r; i++) {
                sb.append(" ");
            }
            sb.append(r < 9 ? (char) ('1' + r) : r == 9 ? "T" : "E").append(" ");
            for (int c = 0; c < 11; c++) {
                Cell currentCell = board[r][c];
                if (currentCell.player == 0) {
                    sb.append(".");
                } else {
                    char displayChar = currentCell.player == 1 ? 'b' : 'r';
                    // Check if currentCell is in the winning group and convert to uppercase if so
                    if (winningGroup != null && winningGroup.cells.contains(currentCell)) {
                        displayChar = Character.toUpperCase(displayChar);
                    }
                    sb.append(displayChar);
                }
                if (c < 10) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        if (isGameOver) {
            getValue();
        }
        return sb.toString();
    }

}