package BoardGames.Peg5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import BoardGames.Board_Interface.Board;

public class Peg5Board implements Board {
    public class Peg5Move implements Board.Move {
        // Member variables and implementations for Move interface methods

        private Position from;
        private Position to;
        private int pieceType; // This could be encoded according to the game's rules
        public int row;
        public int col;

        @Override
        public void write(OutputStream os) throws IOException {
            DataOutputStream dos = new DataOutputStream(os);
            // Example serialization: write positions and piece type
            dos.writeInt(from.getRow());
            dos.writeInt(from.getColumn());
            dos.writeInt(to.getRow());
            dos.writeInt(to.getColumn());
            dos.writeInt(pieceType);
        }

        @Override
        public void read(InputStream is) throws IOException {
            DataInputStream dis = new DataInputStream(is);
            // Example deserialization: read positions and piece type
            from = new Position(dis.readInt(), dis.readInt(), col, 0);
            to = new Position(dis.readInt(), dis.readInt(), col, 0);
            pieceType = dis.readInt();
        }

        @Override
        public void fromString(String s) {
            // Implement parsing from a string representation
        }

        @Override
        public int compareTo(Move o) {
            // Implement comparison logic, perhaps based on move impact or other criteria
            return 0; // Placeholder return
        }

    }

    private int SIZE; // The size of the game board, typically 7x7 for Peg5.
    private int currentPlayer; // Tracks the current player. Could be 1 for Player 1 and -1 for Player 2.
    private List<Peg5Move> moveHistory; // History of all moves made during the game for undo functionality.
    private int[][] board; // The game board, where each cell contains a code representing a piece or
                           // empty.

    // Arrays to track the pieces each player has:
    private byte[] greenPegs;
    private byte[] greenClosedTubes;
    private byte[] greenOpenTubes;
    private byte[] yellowPegs;
    private byte[] yellowClosedTubes;
    private byte[] yellowOpenTubes;

    /**
     * Constructs a new Peg5Board, initializing the board to empty and setting the
     * current player.
     */
    public Peg5Board() {
        SIZE = 7; // Defaulting the board size to 7x7
        board = new int[SIZE][SIZE];
        moveHistory = new ArrayList<>();
        currentPlayer = 1; // Assuming player 1 starts
        initializePieces();
    }

    /**
     * Initializes arrays representing the pieces for each player. Each type of
     * piece
     * has a specific number in accordance with the game rules.
     */
    private void initializePieces() {
        greenPegs = new byte[10];
        greenClosedTubes = new byte[4];
        greenOpenTubes = new byte[4];
        yellowPegs = new byte[10];
        yellowClosedTubes = new byte[4];
        yellowOpenTubes = new byte[4];
    }

    @Override
    public Move createMove() {
        return new Peg5Move();
    }

    @Override
    /**
     * This method applies a player's move to the Peg5 game board. A move consists
     * of
     * transferring a piece from one position (from) to another (to). The move is
     * encapsulated
     * in an instance of Peg5Move, which includes both the start and end positions
     * and the type
     * of piece being moved.
     *
     * Steps to apply a move:
     * 1. Type Checking: Verify that the move instance passed to the method is of
     * type Peg5Move.
     * Throw an InvalidMoveException if it is not to ensure type safety.
     *
     * 2. Position Validation:
     * a. Check if both the starting (from) and ending (to) positions are within the
     * boundaries
     * of the game board using the withinBoardLimits helper method. The game board
     * is a
     * two-dimensional grid defined by SIZE (typically 7x7 for Peg5). If either
     * position
     * is out of bounds, throw an InvalidMoveException with a message indicating the
     * move
     * is out of board limits.
     *
     * b. Ensure that the destination position (to) is empty (i.e., does not contain
     * any other pieces).
     * In Peg5, a position on the board can only hold one piece at a time unless
     * merging specific
     * types of pieces according to game rules (not covered in this method
     * directly). If the destination
     * is not empty, throw an InvalidMoveException stating that the cell is
     * occupied.
     *
     * 3. Game-Specific Rules Validation:
     * Use the isValidGameMove method to check additional game-specific conditions
     * such as:
     * - Ensuring the move is made according to the allowed patterns (e.g., only
     * moving to adjacent,
     * connected cells if required by game rules).
     * - Verifying that the move does not violate any special conditions like
     * jumping over other pieces
     * unless explicitly allowed.
     * If the move fails this validation, throw an InvalidMoveException with a
     * description of the violated rule.
     *
     * 4. Applying the Move:
     * - Update the board's state by moving the piece type from the start position
     * to the end position
     * in the board array. This involves setting the destination cell to the piece
     * type and clearing
     * the source cell.
     * - Add the move to the moveHistory list to maintain a record of all moves,
     * which can be used for
     * features like undoing moves or replaying the game sequence.
     *
     * 5. Post-Move Processing:
     * - Call the checkForGroups method to evaluate if the move has created any
     * groups of pieces that
     * meet the winning criteria. This check could involve scanning rows, columns,
     * and diagonals from
     * the position of the newly placed piece.
     *
     * 6. Player Switch:
     * - After a valid move is applied, switch the active player using the
     * switchPlayer method. This
     * toggles the currentPlayer field between two players (typically represented by
     * 1 and -1 in a
     * two-player game).
     *
     * This method is critical for advancing the game state and ensuring the game's
     * rules and integrity
     * are maintained at each step of play.
     */

    public void applyMove(Move m) throws InvalidMoveException {
        Peg5Move move = (Peg5Move) m;
        // 1. Type Checking
        if (move == null) {
            throw new InvalidMoveException("Invalid move type");
        }

        // 2. Position Validation
        if (!withinBoardLimits(move.from) || !withinBoardLimits(move.to)) {
            throw new InvalidMoveException("Move out of board limits");
        }

        if (board[move.to.getRow()][move.to.getColumn()] != 0) {
            throw new InvalidMoveException("Destination cell is occupied");
        }

        // 3. use getValidMoves to check if the move is valid
        if (!getValidMoves().contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // 4. Applying the Move
        if (board[move.from.getRow()][move.from.getColumn()] == 0) {
            // Place a piece on the board
            board[move.to.getRow()][move.to.getColumn()] = currentPlayer;
        } else {
            // Move a piece on the board
            board[move.to.getRow()][move.to.getColumn()] = board[move.from.getRow()][move.from.getColumn()];
            board[move.from.getRow()][move.from.getColumn()] = 0;
        }

        board[move.to.getRow()][move.to.getColumn()] = board[move.from.getRow()][move.from.getColumn()];
        board[move.from.getRow()][move.from.getColumn()] = 0;
        moveHistory.add(move);

        // 5. Post-Move Processing
        checkForGroups(move.to);

        // 6. Player Switch
        switchPlayer();
    }

    /**
     * Scans the board to check for groups of pieces that could potentially form a
     * winning condition.
     * This method looks at horizontal, vertical, and diagonal sequences on the
     * board, identifying any
     * continuous sequence of five pieces.
     *
     * The check is performed as follows:
     * 1. Horizontal Check: For each row, look at every consecutive sequence of five
     * cells.
     * 2. Vertical Check: For each column, repeat the process used in the horizontal
     * check.
     * 3. Diagonal Check: Two types are checked - from top left to bottom right, and
     * top right to bottom left.
     *
     * Each sequence is evaluated against fixed patterns that define a win. If a
     * potential win is detected,
     * the method will update the game state or perform necessary actions such as
     * marking these groups for
     * further evaluation.
     */
    private void checkForGroups(Position to) {
        // Horizontal Check
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE - 4; col++) {
                if (board[row][col] == currentPlayer && board[row][col + 1] == currentPlayer
                        && board[row][col + 2] == currentPlayer && board[row][col + 3] == currentPlayer
                        && board[row][col + 4] == currentPlayer) {
                    // create a group of pieces
                    Group group = new Group();
                    // add pieces to the group
                    for (int i = 0; i < 5; i++) {
                        group.addPiece(new Position(row, col + i, currentPlayer, board[row][col + i]));
                    }
                }
            }
        }

        // Vertical Check
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE - 4; row++) {
                if (board[row][col] == currentPlayer && board[row + 1][col] == currentPlayer
                        && board[row + 2][col] == currentPlayer && board[row + 3][col] == currentPlayer
                        && board[row + 4][col] == currentPlayer) {
                    // create a group of pieces
                    Group group = new Group();
                    // add pieces to the group
                    for (int i = 0; i < 5; i++) {
                        group.addPiece(new Position(row + i, col, currentPlayer, board[row + i][col]));
                    }
                }
            }
        }

        // Diagonal Check (Top Left to Bottom Right)
        for (int row = 0; row < SIZE - 4; row++) {
            for (int col = 0; col < SIZE - 4; col++) {
                if (board[row][col] == currentPlayer && board[row + 1][col + 1] == currentPlayer
                        && board[row + 2][col + 2] == currentPlayer && board[row + 3][col + 3] == currentPlayer
                        && board[row + 4][col + 4] == currentPlayer) {
                    // create a group of pieces
                    Group group = new Group();
                    // add pieces to the group
                    for (int i = 0; i < 5; i++) {
                        group.addPiece(new Position(row + i, col + i, currentPlayer, board[row + i][col + i]));
                    }
                }
            }
        }

        // Diagonal Check (Top Right to Bottom Left)
        for (int row = 0; row < SIZE - 4; row++) {
            for (int col = SIZE - 1; col >= 4; col--) {
                if (board[row][col] == currentPlayer && board[row + 1][col - 1] == currentPlayer
                        && board[row + 2][col - 2] == currentPlayer && board[row + 3][col - 3] == currentPlayer
                        && board[row + 4][col - 4] == currentPlayer) {
                    // create a group of pieces
                    Group group = new Group();
                    // add pieces to the group
                    for (int i = 0; i < 5; i++) {
                        group.addPiece(new Position(row + i, col - i, currentPlayer, board[row + i][col - i]));
                    }
                }
            }
        }
    }

    private boolean withinBoardLimits(Position from) {
        return from.getRow() >= 0 && from.getRow() < SIZE && from.getColumn() >= 0 && from.getColumn() < SIZE;
    }

    @Override
    public int getValue() {
        // Implementation depends on Peg5 game logic for evaluating the board state
        return 0; // Placeholder return
    }

    @Override
    /**
     * Generates and returns a list of all valid moves for the current player. This
     * method
     * considers the entire board state and current player's pieces to determine
     * possible
     * and legal moves according to Peg5 rules.
     *
     * Steps to determine valid moves:
     * 1. Initialize an empty list to store all valid moves.
     * 2. Iterate over each cell of the board to examine potential moves:
     * - For each cell, determine if it can be a starting point for any legal move
     * based on the piece it contains
     * and who controls the piece.
     * 3. Check each possible move from the current cell:
     * - For pieces that can move, consider all potential target cells they could
     * legally move to.
     * - For pieces that can be placed (if the player still has pieces to place),
     * consider all empty cells where
     * a piece can be placed according to game rules.
     * 4. For each potential move, validate the move:
     * - Ensure the move does not result in self-check or any other illegal state.
     * - Confirm the move follows the specific movement and placement rules of Peg5,
     * such as moving within straight lines or placing next to a specific type of
     * piece if required.
     * 5. If the move is valid, add it to the list of valid moves.
     * 6. Return the list of valid moves.
     *
     * This method is crucial for AI implementations and for enabling user choices
     * in interactive game sessions,
     * ensuring that all presented moves are possible within the current game rules
     * and state.
     */
    public List<Peg5Move> getValidMoves() {
        List<Peg5Move> validMoves = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == currentPlayer) {
                    // Check for valid moves from this position
                    // Add valid moves to the list
                }
            }
        }
        return validMoves;
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public List<Peg5Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    @Override
    public void undoMove() {

    }

    public void switchPlayer() {
        currentPlayer = -currentPlayer; // Toggle between PLAYER_0 and PLAYER_1
    }

    public Integer[][] getBoard() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBoard'");
    }

    @Override

    public String toString() {

    }

    // Inner class Peg5Move

}
