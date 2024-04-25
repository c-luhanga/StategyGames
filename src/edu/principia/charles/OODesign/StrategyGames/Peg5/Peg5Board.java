package edu.principia.charles.OODesign.StrategyGames.Peg5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.principia.OODesign.StrategyGames.Board.Board;

public class Peg5Board implements Board {
    public class Peg5Move implements Board.Move, Serializable {
        private static final long serialVersionUID = 1L;

        public static byte type; // Type of piece (peg, open tube, closed tube)
        public Position position; // Target position of the move
        private boolean isTransfer; // Indicates if this is a transfer move
        private Position fromPosition; // Original position for transfer moves

        // Constructors
        public Peg5Move() {
            this((byte) 0, new Position(0, 0, type), false, new Position(0, 0, type));
        }

        public Peg5Move(byte type, Position position, boolean isTransfer, Position fromPosition) {
            this.type = type;
            this.position = position;
            this.isTransfer = isTransfer;
            this.fromPosition = fromPosition;
        }

        // Getters and setters for encapsulation
        public byte getType() {
            return type;
        }

        public Position getPosition() {
            return position;
        }

        public boolean isTransfer() {
            return isTransfer;
        }

        public Position getFromPosition() {
            return fromPosition;
        }

        @Override
        public void write(OutputStream os) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(isTransfer ? 17 : 9);
            buffer.put(type).putInt(position.row).putInt(position.col).put((byte) (isTransfer ? 1 : 0));
            if (isTransfer) {
                buffer.putInt(fromPosition.row).putInt(fromPosition.col);
            }
            os.write(buffer.array());

        }

        @Override
        public void read(InputStream is) throws IOException {
            byte[] data = new byte[isTransfer ? 17 : 9];
            is.read(data);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            type = buffer.get();
            position = new Position(buffer.getInt(), buffer.getInt(), type);
            isTransfer = buffer.get() == 1;
            if (isTransfer) {
                fromPosition = new Position(buffer.getInt(), buffer.getInt(), type);
            }
        }

        @Override
        public void fromString(String s) throws IOException {
            try {
                s = s.trim(); // Trim whitespace
                String[] parts = s.split("\\s+"); // Split by whitespace

                // Extract move type from string
                String typePart = parts[0].toLowerCase();
                this.type = switch (typePart) {
                    case "peg" -> currentPlayer == PLAYER_0 ? GREEN_PEG : YELLOW_PEG;
                    case "open" -> currentPlayer == PLAYER_0 ? OPEN_GREEN_TUBE : OPEN_YELLOW_TUBE;
                    case "closed" -> currentPlayer == PLAYER_0 ? CLOSED_GREEN_TUBE : CLOSED_YELLOW_TUBE;
                    default -> throw new IllegalArgumentException("Unknown piece type: " + typePart);
                };

                // Parse position from the string (assuming format "(x,y)")
                String positionPart = parts[1].replaceAll("[()]", "");
                String[] coordinates = positionPart.split(",");
                int row = Integer.parseInt(coordinates[0].trim()) - 1;
                int col = Integer.parseInt(coordinates[1].trim()) - 1;
                this.position = new Position(row, col, type);

                // Check if there is a transfer part (indicated by "<-")
                if (s.contains("<-")) {
                    this.isTransfer = true;
                    String fromPositionPart = parts[3].replaceAll("[()]", "");
                    coordinates = fromPositionPart.split(",");
                    int fromRow = Integer.parseInt(coordinates[0].trim()) - 1;
                    int fromCol = Integer.parseInt(coordinates[1].trim()) - 1;
                    this.fromPosition = new Position(fromRow, fromCol, type);
                } else {
                    this.isTransfer = false;
                }
            } catch (Exception e) {
                throw new IOException("Invalid move format: " + s, e);
            }
        }

        @Override
        public int compareTo(Move o) {
            if (!(o instanceof Peg5Move)) {
                return 0;
            }
            Peg5Move other = (Peg5Move) o;
            // Compare rows first
            int rowComparison = Integer.compare(this.position.row, other.position.row);
            if (rowComparison != 0)
                return rowComparison;
            // If rows are the same, compare columns
            int colComparison = Integer.compare(this.position.col, other.position.col);
            if (colComparison != 0)
                return colComparison;
            // If positions are the same, compare types
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
            return switch (type) {
                case 1 -> "Peg";
                case 2 -> "Peg";
                case 3 -> "Tube";
                case 4 -> "Tube";
                case 5 -> "Tube";
                case 6 -> "Tube";
                default -> "Unknown";
            };
        }
    }

    public static final int BOARD_SIZE = 7;
    public byte[][] board;
    private int currentPlayer;
    private List<Peg5Move> moveHistory;
    private GroupManager groupManager;

    private static final byte NONE = 0;
    public static final byte GREEN_PEG = 1;
    private static final byte YELLOW_PEG = 2;
    private static final byte OPEN_GREEN_TUBE = 3;
    private static final byte CLOSED_GREEN_TUBE = 4;
    private static final byte OPEN_YELLOW_TUBE = 5;
    private static final byte CLOSED_YELLOW_TUBE = 6;

    private int[] greenUnplayedPegs = { 10 }; // Initially 10 green pegs
    private int[] yellowUnplayedPegs = { 10 }; // Initially 10 yellow pegs
    private int[] greenUnplayedOpenTubes = { 4 }; // Initially 4 green open tubes
    private int[] greenUnplayedClosedTubes = { 4 }; // Initially 4 green closed tubes
    private int[] yellowUnplayedOpenTubes = { 4 }; // Initially 4 yellow open tubes
    private int[] yellowUnplayedClosedTubes = { 4 }; // Initially 4 yellow closed tubes

    private static byte encodePiece(byte peg, byte tube) {
        return (byte) ((peg & 0x07) | (tube << 3));
    }

    private static byte decodePeg(byte piece) {
        return (byte) (piece & 0x07);
    }

    private static byte decodeTube(byte piece) {
        return (byte) ((piece >> 3) & 0x07);
    }

    /**
     * Constructs a new Peg5Board, initializing the board to empty and setting the
     * current player.
     */
    public Peg5Board() {
        this.board = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = encodePiece(NONE, NONE); // All cells initialized to empty
            }
        }
        this.currentPlayer = PLAYER_0; // Game starts with player 0
        this.moveHistory = new ArrayList<>();
        this.groupManager = new GroupManager(this);
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
        decrementPieceCount(move);

        // Validate and apply the move
        if (!isMoveValid(move, move.position.row, move.position.col)) {
            throw new InvalidMoveException("Move is not valid");
        }
        updateBoard(move);
        groupManager.updateGroupsAfterMove(move.position, move.type);
        moveHistory.add(move);
        currentPlayer = -currentPlayer;

        // After move application, check for win condition
        if (checkForWinCondition()) {
            System.out.println("Win detected!");
        }
    }

    private boolean checkForWinCondition() {
        for (Group group : groupManager.groups) {
            if (WinPatterns.isWinningLine(group.getPositionsAsLine(), (byte) currentPlayer)) {
                return true;
            }
        }
        return false;
    }

    private void decrementPieceCount(Peg5Move move) {
        int[] unplayedPieces = currentPlayer == PLAYER_0 ? greenUnplayedPegs : yellowUnplayedPegs;
        switch (move.type) {
            case GREEN_PEG:
            case YELLOW_PEG:
                unplayedPieces[0]--;
                break;
            case OPEN_GREEN_TUBE:
            case OPEN_YELLOW_TUBE:
                unplayedPieces[1]--;
                break;
            case CLOSED_GREEN_TUBE:
            case CLOSED_YELLOW_TUBE:
                unplayedPieces[2]--;
                break;
        }
    }

    private void updateBoard(Peg5Move move) {
        byte originContent = move.isTransfer ? board[move.fromPosition.row][move.fromPosition.col] : NONE;
        byte destContent = board[move.position.row][move.position.col];

        // Clear the origin only if it's a transfer move
        if (move.isTransfer) {
            // Determine what remains at the source location after the move
            byte remainingPeg = decodePeg(originContent);
            byte remainingTube = decodeTube(originContent);

            if (move.type == GREEN_PEG || move.type == YELLOW_PEG) {
                remainingPeg = NONE; // The peg is moving, tube may stay
            } else if (move.type == OPEN_GREEN_TUBE || move.type == OPEN_YELLOW_TUBE || move.type == CLOSED_GREEN_TUBE
                    || move.type == CLOSED_YELLOW_TUBE) {
                remainingTube = NONE; // The tube is moving, peg may stay
            }

            // Update the source cell
            board[move.fromPosition.row][move.fromPosition.col] = encodePiece(remainingPeg, remainingTube);
        }

        // Determine new content at the destination
        byte newPeg = decodePeg(destContent);
        byte newTube = decodeTube(destContent);

        if (move.type == GREEN_PEG || move.type == YELLOW_PEG) {
            newPeg = move.type; // Place the peg
        } else if (move.type == OPEN_GREEN_TUBE || move.type == OPEN_YELLOW_TUBE) {
            newTube = move.type; // Place an open tube
        } else if (move.type == CLOSED_GREEN_TUBE || move.type == CLOSED_YELLOW_TUBE) {
            if (destContent == NONE) {
                newTube = move.type; // Only place closed tube if destination is empty
            } else {
                // If trying to place a closed tube on a non-empty cell, raise an exception
                System.out.print("Cannot place a closed tube on a non-empty cell.");
            }
        }

        // Update the destination cell with the new configuration
        board[move.position.row][move.position.col] = encodePiece(newPeg, newTube);
    }

    private boolean isMoveValid(Peg5Move move, int row, int col) {
        System.out.println("Validating Move: " + move);

        // Check if the specified positions are within the bounds of the board
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            System.out.println("Move out of bounds");
            return false;
        }

        // Decode the source and destination pieces if it's a transfer, otherwise
        // consider the destination directly
        byte destPiece = board[row][col];
        byte sourcePiece = move.isTransfer ? board[move.fromPosition.row][move.fromPosition.col] : NONE;

        // Ensure the destination is compatible with the intended move
        if (!isCompatible(sourcePiece, destPiece, move.isTransfer)) {
            System.out.println("Incompatible pieces");
            return false;
        }

        // Check if the placement or movement is valid based on the piece type
        if (!isPlacementValid(move.type, row, col, destPiece)) {
            System.out.println("Placement invalid");
            return false;
        }

        System.out.println("Move valid");
        return true;
    }

    private boolean isPlacementValid(byte type, int row, int col, byte destPiece) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            System.out.println("Move out of bounds");
            return false;
        }

        byte destContent = board[row][col];
        byte destPeg = decodePeg(destContent);
        byte destTube = decodeTube(destContent);

        switch (type) {
            case GREEN_PEG:
            case YELLOW_PEG:
                // Pegs can be placed in an empty space or on any open tube
                return destPeg == NONE
                        && (destTube == NONE || destTube == OPEN_GREEN_TUBE || destTube == OPEN_YELLOW_TUBE);
            case OPEN_GREEN_TUBE:
            case OPEN_YELLOW_TUBE:
                // Open tubes can be placed in an empty space or on any peg
                return destTube == NONE && (destPeg == NONE || destPeg == GREEN_PEG || destPeg == YELLOW_PEG);
            case CLOSED_GREEN_TUBE:
            case CLOSED_YELLOW_TUBE:
                // Closed tubes can only be placed in an empty space
                return destPeg == NONE && destTube == NONE;
            default:
                System.out.println("Unknown piece type: " + type);
                return false;
        }
    }

    private boolean isCompatible(byte sourcePiece, byte destPiece, boolean isTransfer) {
        if (!isTransfer) {
            return (destPiece == NONE);
        } else {
            byte sourcePeg = decodePeg(sourcePiece);
            byte sourceTube = decodeTube(sourcePiece);
            byte destPeg = decodePeg(destPiece);
            byte destTube = decodeTube(destPiece);

            // If transferring a peg, destination can only be an empty space or have a tube
            if (sourcePeg != NONE && (destPeg == NONE || destTube != NONE)) {
                return true;
            }
            // If transferring an open tube, destination can only be an empty space or have
            // a peg
            if (sourceTube != NONE && (destTube == NONE || destPeg != NONE)) {
                return true;
            }
            // If transferring a closed tube, destination can only be an empty space (no peg
            // or tube)
            if (sourceTube == NONE && sourcePeg == NONE && destPeg == NONE && destTube == NONE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getValue() {
        System.out.println("Evaluating board value");
        int score = 0;
        for (Group group : groupManager.groups) {
            score += WinPatterns.evaluateLineScore(group.getPositionsAsLine(), (byte) currentPlayer);
        }
        return score;
    }

    @Override
    public List<Peg5Move> getValidMoves() {
        List<Peg5Move> validMoves = new ArrayList<>();
        byte[] pieceTypes = getCurrentPlayerPieceTypes(); // Get the appropriate piece types for the current player

        // Iterate over all board positions in row-major order
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                byte pieceAtPosition = board[row][col];
                // Generate placement moves if the position is empty
                if (pieceAtPosition == 0) {
                    for (byte pieceType : pieceTypes) {
                        validMoves.add(new Peg5Move(pieceType, new Position(row, col, pieceType), false, null));
                    }
                }
                // Generate transfer moves if all pieces of the type have been placed
                else if (allPiecesPlaced(pieceAtPosition)) {
                    addTransferMoves(row, col, validMoves);
                }
            }
        }

        return validMoves; // Moves are added in the correct order, no need to sort
    }

    private byte[] getCurrentPlayerPieceTypes() {
        if (currentPlayer == PLAYER_0) {
            return new byte[] { GREEN_PEG, OPEN_GREEN_TUBE, CLOSED_GREEN_TUBE };
        } else {
            return new byte[] { YELLOW_PEG, OPEN_YELLOW_TUBE, CLOSED_YELLOW_TUBE };
        }
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
                            .add(new Peg5Move(pieceType, new Position(row, col, pieceType), true,
                                    new Position(fromRow, fromCol, pieceType)));
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
            return; // No move to undo if history is empty
        }

        Peg5Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Retrieve the piece types from the encoded board state
        byte pieceAtDest = board[lastMove.position.row][lastMove.position.col];
        byte pegAtDest = decodePeg(pieceAtDest);
        byte tubeAtDest = decodeTube(pieceAtDest);

        // Restore the board state
        if (lastMove.isTransfer) {
            // Move the piece back to its original position
            byte pieceAtSource = board[lastMove.fromPosition.row][lastMove.fromPosition.col];
            board[lastMove.position.row][lastMove.position.col] = encodePiece(decodePeg(pieceAtSource),
                    decodeTube(pieceAtSource));
            board[lastMove.fromPosition.row][lastMove.fromPosition.col] = encodePiece(pegAtDest, tubeAtDest);
        } else {
            // Clear the destination position since it was not a transfer
            board[lastMove.position.row][lastMove.position.col] = encodePiece(NONE, NONE);
        }

        // Increment the count of the unplayed pieces
        // This assumes that we decrement the count on applyMove; we need to increment
        // it back here
        if (currentPlayer == PLAYER_0) {
            switch (lastMove.type) {
                case GREEN_PEG:
                    greenUnplayedPegs[0]++;
                    break;
                case OPEN_GREEN_TUBE:
                    greenUnplayedOpenTubes[0]++;
                    break;
                case CLOSED_GREEN_TUBE:
                    greenUnplayedClosedTubes[0]++;
                    break;
            }
        } else {
            switch (lastMove.type) {
                case YELLOW_PEG:
                    yellowUnplayedPegs[0]++;
                    break;
                case OPEN_YELLOW_TUBE:
                    yellowUnplayedOpenTubes[0]++;
                    break;
                case CLOSED_YELLOW_TUBE:
                    yellowUnplayedClosedTubes[0]++;
                    break;
            }
        }
        // Switch back to the previous player
        groupManager.undoLastMove(lastMove);

        currentPlayer = -currentPlayer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Display Green's unplayed pieces
        sb.append(String.format("%10s", String.join("", Collections.nCopies(greenUnplayedPegs[0], "G"))))
                .append("\n");
        sb.append(String.format("%10s", String.join(" ", Collections.nCopies(greenUnplayedOpenTubes[0], "Og"))))
                .append(" ")
                .append(String.format("%10s", String.join(" ", Collections.nCopies(greenUnplayedClosedTubes[0], "-g"))))
                .append("\n\n");

        // Display the board with column headers
        sb.append("   "); // Space for row labels
        for (int i = 1; i <= BOARD_SIZE; i++) {
            sb.append(String.format("%3d", i));
        }
        sb.append("\n");

        for (int row = 0; row < BOARD_SIZE; row++) {
            sb.append(String.format("%2d ", row + 1)); // Row label
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(String.format("%3s", pieceToString(board[row][col])));
            }
            sb.append("\n");
        }
        sb.append("\n");

        // Display Yellow's unplayed pieces
        sb.append(String.format("%10s", String.join(" ", Collections.nCopies(yellowUnplayedOpenTubes[0], "Oy"))))
                .append(" ")
                .append(String.format("%10s",
                        String.join(" ", Collections.nCopies(yellowUnplayedClosedTubes[0], "-y"))))
                .append("\n");
        sb.append(String.format("%10s", String.join("", Collections.nCopies(yellowUnplayedPegs[0], "Y"))))
                .append("\n\n");

        // Display the current player's turn
        sb.append(currentPlayer == PLAYER_0 ? "Green to play" : "Yellow to play")
                .append("\n");

        return sb.toString();
    }

    private String pieceToString(byte piece) {
        // int peg = decodePeg(piece);
        // int tube = decodeTube(piece);
        System.out.println("Peg: " + piece + " Tube: " + piece);
        // Combine peg and tube information into a single string representation
        switch (piece) {
            case OPEN_GREEN_TUBE:
                return (piece == GREEN_PEG ? "Gy" : (piece == YELLOW_PEG ? "Yg" : "Og"));
            case CLOSED_GREEN_TUBE:
                return (piece == GREEN_PEG ? "G-" : (piece == YELLOW_PEG ? "Y-" : "-g"));
            case OPEN_YELLOW_TUBE:
                return (piece == YELLOW_PEG ? "Yg" : (piece == GREEN_PEG ? "Gy" : "Oy"));
            case CLOSED_YELLOW_TUBE:
                return (piece == YELLOW_PEG ? "Y-" : (piece == GREEN_PEG ? "G-" : "-y"));
            default:
                switch (piece) {
                    case GREEN_PEG:
                        return "G";
                    case YELLOW_PEG:
                        return "Y";
                    default:
                        return ".";
                }
        }
    }
}
