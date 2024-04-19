package BoardGames.Peg5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import BoardGames.Board_Interface.Board;

public class Peg5Board implements Board {
    public class Peg5Move implements Board.Move, Serializable {
        public byte type; // Type of piece (peg, open tube, closed tube)
        public Position position; // Target position of the move
        public boolean isTransfer; // Indicates if this is a transfer move
        public Position fromPosition; // Original position for transfer moves

        // Constructors
        public Peg5Move() {
            this.type = 0;
            this.position = new Position(currentPlayer, currentPlayer);
            this.isTransfer = false;
            this.fromPosition = new Position(currentPlayer, currentPlayer);
        }

        public Peg5Move(byte type, Position position, boolean isTransfer, Position fromPosition) {
            this.type = type;
            this.position = position;
            this.isTransfer = isTransfer;
            this.fromPosition = fromPosition;
        }

        @Override
        public void write(OutputStream os) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(10); // Allocate enough space
            buffer.put(type);
            buffer.putInt(position.getRow());
            buffer.putInt(position.getColumn());
            if (isTransfer) {
                buffer.put((byte) 1); // Indicate transfer
                buffer.putInt(fromPosition.getRow());
                buffer.putInt(fromPosition.getColumn());
            } else {
                buffer.put((byte) 0); // Not a transfer
            }
            os.write(buffer.array());
        }

        @Override
        public void read(InputStream is) throws IOException {
            byte[] data = new byte[10]; // Match the write size
            is.read(data);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            this.type = buffer.get();
            int row = buffer.getInt();
            int col = buffer.getInt();
            this.position = new Position(row, col);
            byte transferFlag = buffer.get();
            if (transferFlag == 1) {
                this.isTransfer = true;
                row = buffer.getInt();
                col = buffer.getInt();
                this.fromPosition = new Position(row, col);
            } else {
                this.isTransfer = false;
            }
        }

        @Override
        public void fromString(String s) throws IOException {
            try {
                s = s.trim(); // Remove any leading or trailing spaces
                String[] parts = s.split("\\s+"); // Split by whitespace
                String typePart = parts[0];

                // Determine the type based on the string
                switch (typePart.toLowerCase()) {
                    case "peg":
                        this.type = 1; // Assume 1 for peg
                        break;
                    case "open":
                        this.type = 2; // Assume 2 for open tube
                        break;
                    case "closed":
                        this.type = 3; // Assume 3 for closed tube
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown piece type: " + typePart);
                }

                // Parse the position part, assuming format "(row,col)"
                String positionPart = parts[1];
                positionPart = positionPart.replaceAll("[()]", ""); // Remove parentheses
                String[] coordinates = positionPart.split(",");
                int row = Integer.parseInt(coordinates[0].trim()) - 1; // Convert to 0-based index
                int col = Integer.parseInt(coordinates[1].trim()) - 1; // Convert to 0-based index
                this.position = new Position(row, col);

                // Check if there is a transfer part
                if (s.contains("<-")) {
                    this.isTransfer = true;
                    String fromPositionPart = parts[3]; // Part after "<-"
                    fromPositionPart = fromPositionPart.replaceAll("[()]", ""); // Remove parentheses
                    coordinates = fromPositionPart.split(",");
                    int fromRow = Integer.parseInt(coordinates[0].trim()) - 1; // Convert to 0-based index
                    int fromCol = Integer.parseInt(coordinates[1].trim()) - 1; // Convert to 0-based index
                    this.fromPosition = new Position(fromRow, fromCol);
                } else {
                    this.isTransfer = false;
                }
            } catch (Exception e) {
                throw new IOException("Invalid move format: " + s, e);
            }
        }

        @Override
        public int compareTo(Move o) {
            Peg5Move other = (Peg5Move) o;
            // First compare by position, then by type
            int rowCompare = Integer.compare(this.position.getRow(), other.position.getRow());
            if (rowCompare != 0)
                return rowCompare;
            int colCompare = Integer.compare(this.position.getColumn(), other.position.getColumn());
            if (colCompare != 0)
                return colCompare;
            return Byte.compare(this.type, other.type);
        }

        @Override
        public String toString() {

            String typeName = getTypeName(); // Convert type to string
            String pos = String.format("(%d,%d)", position.row + 1, position.col + 1); // 1-based index

            if (isTransfer) {
                String fromPos = String.format("(%d,%d)", fromPosition.row + 1, fromPosition.col + 1); // 1-based index
                return String.format("%s %s <- %s", typeName, pos, fromPos);
            } else {
                return String.format("%s %s", typeName, pos);
            }
        }

        private String getTypeName() {
            switch (this.type) {
                case 1:
                    return "peg";
                case 2:
                    return "open";
                case 3:
                    return "closed";
                default:
                    return "unknown";
            }

        }
    }

    public static final int BOARD_SIZE = 7;
    public byte[][] board;
    public int currentPlayer;
    public List<Peg5Move> moveHistory;
    public List<Group> activeGroups; // List to keep track of active groups

    /**
     * Constructs a new Peg5Board, initializing the board to empty and setting the
     * current player.
     */
    public Peg5Board() {
        this.board = new byte[BOARD_SIZE][BOARD_SIZE];
        this.currentPlayer = PLAYER_0;
        this.moveHistory = new ArrayList<>();
        this.activeGroups = new ArrayList<>();
    }

    @Override
    public Move createMove() {
        return new Peg5Move();
    }

    @Override
    public void applyMove(Move m) throws InvalidMoveException {
        if (!(m instanceof Peg5Move)) {
            throw new InvalidMoveException("Invalid move class");
        }
        Peg5Move move = (Peg5Move) m;
        if (isMoveValid(move, move.position.row, move.position.col)) {
            updateBoard(move);
            moveHistory.add(move);
            updateGroups(move);
            currentPlayer = -currentPlayer; // Switch players
        } else {
            throw new InvalidMoveException("Move is not valid");
        }
    }

    private void updateGroups(Peg5Move move) {
        // Apply move to board, updating pieces
        board[move.position.row][move.position.col] = move.type;
        if (move.isTransfer) {
            board[move.fromPosition.row][move.fromPosition.col] = 0; // Clear old position
        }
    }

    private void updateBoard(Peg5Move move) {
        // Apply move to board, updating pieces
        board[move.position.row][move.position.col] = move.type;
        if (move.isTransfer) {
            board[move.fromPosition.row][move.fromPosition.col] = 0; // Clear old position
        }
    }

    private boolean isMoveValid(Peg5Move move, int row, int col) {
        // Check bounds
        if (!(row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)) {
            return false;
        }

        if (move.isTransfer) {
            // For transfer moves, check that the fromPosition is within bounds and contains
            // the player's piece
            if (!(row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) ||
                    board[move.fromPosition.row][move.fromPosition.col] != move.type ||
                    board[move.position.row][move.position.col] != 0) {
                return false;
            }
        } else {
            // For placement moves, ensure the target cell is empty
            if (board[move.position.row][move.position.col] != 0) {
                return false;
            }
        }

        // Ensure the move corresponds to the current player's piece type
        // Assuming 1, 2 for PLAYER_0 (peg, tube) and 3, 4 for PLAYER_1 (peg, tube)
        if (currentPlayer == PLAYER_0 && move.type > 2) {
            return false;
        } else if (currentPlayer == PLAYER_1 && move.type < 3) {
            return false;
        }

        // All checks passed, the move is valid
        return true;
    }

    @Override
    public int getValue() {
        int greenScore = 0;
        int yellowScore = 0;
        for (Group group : activeGroups) {
            if (group.owner == PLAYER_0) {
                greenScore += group.evaluateScore();
            } else {
                yellowScore += group.evaluateScore();
            }
        }
        return greenScore - yellowScore;
    }

    @Override
    public List<Peg5Move> getValidMoves() {
        List<Peg5Move> validMoves = new ArrayList<>();
        // Define constants for piece types in the order required by the sorting
        // criteria
        byte[] pieceTypes = { 1, 2, 3 }; // 1: peg, 2: open tube, 3: closed tube for PLAYER_0 and adjusted for PLAYER_1

        // Iterate over all board positions in row-major order
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Check if the position is empty for placement moves
                if (board[row][col] == 0) {
                    for (byte pieceType : pieceTypes) {
                        validMoves.add(new Peg5Move(pieceType, new Position(row, col), false, null));
                    }
                } else {
                    // Check for possible transfer moves if all pieces of the type have been placed
                    byte pieceType = board[row][col];
                    if (allPiecesPlaced(pieceType)) {
                        addTransferMoves(row, col, validMoves);
                    }
                }
            }
        }

        return validMoves;
    }

    private boolean allPiecesPlaced(byte pieceType) {
        int count = countPlacedPieces(pieceType);
        return count == 10; // Assuming each player has 10 pieces of each type
    }

    private int countPlacedPieces(byte pieceType) {
        int count = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == pieceType) {
                    count++;
                }
            }
        }
        return count;
    }

    private void addTransferMoves(int fromRow, int fromCol, List<Peg5Move> validMoves) {
        byte pieceType = board[fromRow][fromCol];
        // Add moves transferring a piece from (fromRow, fromCol) to any empty cell
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == 0) {
                    validMoves
                            .add(new Peg5Move(pieceType, new Position(row, col), true, new Position(fromRow, fromCol)));
                }
            }
        }
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
        if (moveHistory.isEmpty()) {
            return; // Nothing to undo
        }
        Peg5Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        // Undo the move by clearing the target position
        board[lastMove.position.row][lastMove.position.col] = 0;
        if (lastMove.isTransfer) {
            // Restore the piece to the original position
            board[lastMove.fromPosition.row][lastMove.fromPosition.col] = lastMove.type;
        }
        // Switch back to the previous player
        currentPlayer = -currentPlayer;
    }

    @Override
    // Player Indicator: Shows which player's turn it is currently ("Green" or
    // "Yellow"). This can be adapted if you're using different identifiers or
    // colors for players.
    // Board Layout: The board is printed with rows and columns labeled for clarity.
    // Each cell of the board is converted into a readable character ,
    // making the board state easy to understand at a glance.
    // Piece Representation: convert numeric piece
    // identifiers into characters ('P', 'O' for different types of pieces and
    // players). You can customize these characters based on your actual game pieces
    // or player distinction.
    // This toString method will provide a clear and immediately useful view of the
    // board state, making it suitable for outputting to a console or debugging
    // tool. This visual representation can be invaluable during development,
    // testing, or even when playing the game in a text-based environment.
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player Indicator: ");
        sb.append(currentPlayer == PLAYER_0 ? "Green" : "Yellow");
        sb.append("\n");
        sb.append("Board Layout:\n");
        sb.append("  1 2 3 4 5 6 7\n");
        for (int row = 0; row < BOARD_SIZE; row++) {
            sb.append(row + 1).append(" ");
            for (int col = 0; col < BOARD_SIZE; col++) {
                byte piece = board[row][col];
                char pieceChar = ' ';
                if (piece == 1) {
                    pieceChar = 'P'; // Peg
                } else if (piece == 2) {
                    pieceChar = 'O'; // Open tube
                } else if (piece == 3) {
                    pieceChar = 'C'; // Closed tube
                }
                sb.append(pieceChar).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
