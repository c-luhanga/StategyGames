package edu.principia.charles.OODesign.StrategyGames.Peg5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.principia.OODesign.StrategyGames.Board.Board;

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
            // Start with 1 byte for the type, 4 bytes each for row and col, plus 1 byte for
            // the transfer flag
            int bufferSize = 1 + 4 + 4 + 1; // Basic move size
            if (isTransfer) {
                // Add additional 8 bytes for the fromPosition if it's a transfer move
                bufferSize += 4 + 4;
            }
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            buffer.put(type);
            buffer.putInt(position.row);
            buffer.putInt(position.col);
            buffer.put((byte) (isTransfer ? 1 : 0)); // Transfer flag
            if (isTransfer) {
                buffer.putInt(fromPosition.row);
                buffer.putInt(fromPosition.col);
            }
            os.write(buffer.array());

        }

        @Override
        public void read(InputStream is) throws IOException {
            // Read the minimum required data first
            byte[] baseData = new byte[1 + 4 + 4 + 1]; // Type, row, col, and transfer flag
            is.read(baseData);
            ByteBuffer buffer = ByteBuffer.wrap(baseData);
            this.type = buffer.get();
            this.position = new Position(buffer.getInt(), buffer.getInt());
            byte transferFlag = buffer.get();

            if (transferFlag == 1) {
                this.isTransfer = true;
                // If it's a transfer, read additional 8 bytes
                byte[] transferData = new byte[4 + 4];
                is.read(transferData);
                ByteBuffer transferBuffer = ByteBuffer.wrap(transferData);
                this.fromPosition = new Position(transferBuffer.getInt(), transferBuffer.getInt());
            } else {
                this.isTransfer = false;
            }
        }

        @Override
        public void fromString(String s) throws IOException {
            try {
                s = s.trim(); // Remove any leading or trailing whitespace
                String[] parts = s.split("\\s+"); // Split by spaces to separate parts

                // Part 0: Move type (e.g., "peg", "open", "closed")
                String typePart = parts[0].toLowerCase();
                switch (typePart) {
                    case "peg":
                        this.type = (currentPlayer == PLAYER_0 ? GREEN_PEG : YELLOW_PEG); // Assuming default to Green
                                                                                          // for simplicity; adjust
                                                                                          // based on
                        // currentPlayer or context
                        break;
                    case "open":
                        this.type = (currentPlayer == PLAYER_0 ? OPEN_GREEN_TUBE : OPEN_YELLOW_TUBE); // Assuming
                                                                                                      // default to
                                                                                                      // Green Open Tube
                        break;
                    case "closed":
                        this.type = (currentPlayer == PLAYER_0 ? CLOSED_GREEN_TUBE : CLOSED_YELLOW_TUBE); // Assuming
                                                                                                          // default to
                                                                                                          // Green
                                                                                                          // Closed Tube
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown piece type: " + typePart);
                }

                // Part 1: Move position (e.g., "(2,3)")
                String positionPart = parts[1].replaceAll("[()]", ""); // Strip parentheses
                String[] coordinates = positionPart.split(",");
                int row = Integer.parseInt(coordinates[0].trim()) - 1; // Convert to 0-based index
                int col = Integer.parseInt(coordinates[1].trim()) - 1; // Convert to 0-based index
                this.position = new Position(row, col);

                // Check if there is a transfer part (indicated by "<-")
                if (s.contains("<-")) {
                    this.isTransfer = true;
                    // Part 3: Original position (e.g., "(5,3)")
                    String fromPositionPart = parts[3].replaceAll("[()]", ""); // Strip parentheses
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
                case 1, 2:
                    return "peg";
                case 3, 5:
                    return "open";
                case 4, 6:
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
    private GroupManager groupManager;
    public static final byte NONE = 0;
    public static final byte GREEN_PEG = 1;
    public static final byte YELLOW_PEG = 2;
    public static final byte OPEN_GREEN_TUBE = 3;
    public static final byte CLOSED_GREEN_TUBE = 4;
    public static final byte OPEN_YELLOW_TUBE = 5;
    public static final byte CLOSED_YELLOW_TUBE = 6;
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
                board[i][j] = encodePiece(NONE, NONE);
            }
        }
        this.currentPlayer = PLAYER_0;
        this.moveHistory = new ArrayList<>();
        this.groupManager = new GroupManager(this);
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
        decrementPieceCount(move);

        // Validate the move first
        if (!isMoveValid(move, move.position.row, move.position.col)) {
            System.out.println("Move not valid: " + move); // Debugging output
            throw new InvalidMoveException("Move is not valid");
        }
        // If the move is valid, apply it to the board
        updateBoard(move);

        groupManager.updateGroupsAfterMove(move);
        // Log the move for undo functionality and history tracking
        moveHistory.add(move);
        // Update any groups or scoring mechanisms if necessary
        updateGroups(move);
        // Switch to the next player
        currentPlayer = -currentPlayer;
    }

    private void decrementPieceCount(Peg5Move move) {
        if (currentPlayer == PLAYER_0) {
            switch (move.type) {
                case GREEN_PEG:
                    greenUnplayedPegs[0]--;
                    break;
                case OPEN_GREEN_TUBE:
                    greenUnplayedOpenTubes[0]--;
                    break;
                case CLOSED_GREEN_TUBE:
                    greenUnplayedClosedTubes[0]--;
                    break;
            }
        } else {
            switch (move.type) {
                case YELLOW_PEG:
                    yellowUnplayedPegs[0]--;
                    break;
                case OPEN_YELLOW_TUBE:
                    yellowUnplayedOpenTubes[0]--;
                    break;
                case CLOSED_YELLOW_TUBE:
                    yellowUnplayedClosedTubes[0]--;
                    break;
            }
        }
    }

    private void updateGroups(Peg5Move move) {
        // Apply move to board, updating pieces
        board[move.position.row][move.position.col] = move.type;

        // Clear old position if it is a transfer move
        if (move.isTransfer) {
            board[move.fromPosition.row][move.fromPosition.col] = 0;
        }
        System.out.println("Updating groups after move: " + move);
        // Assuming 'groupManager' is a field in Peg5Board that manages groups
        groupManager.updateGroupsAfterMove(move);
    }

    private void updateBoard(Peg5Move move) {
        byte originContent = 0; // Assume empty initially
        if (move.isTransfer) {
            // Save the content from the origin position
            originContent = board[move.fromPosition.row][move.fromPosition.col];
            // check if the origin position is a peg with a tube
            if (decodePeg(originContent) != NONE && decodeTube(originContent) != NONE) {
                // If the origin position has both a peg and a tube, we need to move the
                // requested item
                // from the origin to the destination and leave the other item behind
                // check if move being transferred is a peg or tube and move the corresponding
                // item to the destination
                if (move.type <= YELLOW_PEG) {
                    // If the move is a peg, we need to move the tube from the origin to the
                    // destination
                    move.type = decodeTube(originContent);
                } else if (move.type >= OPEN_GREEN_TUBE) {
                    // If the move is a tube, we need to move the peg from the origin to the
                    // destination
                    move.type = decodePeg(originContent);
                }
            } else {
                // Clear the original position since we are transferring the piece
                board[move.fromPosition.row][move.fromPosition.col] = encodePiece(NONE, NONE);
            }
        }

        // Get the current content of the target cell where the piece will be placed or
        // transferred
        byte targetContent = board[move.position.row][move.position.col];
        byte targetPeg = decodePeg(targetContent);
        byte targetTube = decodeTube(targetContent);

        // If transferring, use the peg and tube from the origin if applicable
        if (move.isTransfer) {
            byte originPeg = decodePeg(originContent);
            byte originTube = decodeTube(originContent);
            if (move.type <= YELLOW_PEG) {
                targetPeg = originPeg;
            } else if (move.type >= OPEN_GREEN_TUBE) {
                targetTube = originTube;
            }
        }

        // Calculate the new content of the target position based on the move type
        if (move.type <= YELLOW_PEG) {
            // For pegs, replace the peg part of the target position
            targetPeg = move.type;
        } else if (move.type >= OPEN_GREEN_TUBE) {
            // For tubes, replace the tube part of the target position
            targetTube = move.type;
        }
        // Encode the new state back into the board at the target position
        board[move.position.row][move.position.col] = encodePiece(targetPeg, targetTube);
    }

    private boolean isMoveValid(Peg5Move move, int row, int col) {
        System.out.println("Validating Move: " + move);

        // Check if the specified positions are within the bounds of the board
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            System.out.println("Move out of bounds");

            return false;
        }

        // Decode the source and destination pieces
        byte sourcePiece = move.isTransfer ? board[move.fromPosition.row][move.fromPosition.col] : 0;
        byte destPiece = board[row][col];

        // Ensure the destination is compatible with the intended move
        if (!isCompatible(sourcePiece, destPiece, move.isTransfer)) {
            System.out.println("Incompatible pieces");
            return false;
        }

        // Check if the placement or movement is valid based on the piece type
        if (!isPlacementValid(move.type, row, col, move.isTransfer)) {
            System.out.println("Placement invalid");
            return false;
        }

        System.out.println("Move valid");
        return isMoveTypeValid(move.type);
    }

    private boolean isMoveTypeValid(byte type) {
        if (currentPlayer == PLAYER_0) {
            return type == GREEN_PEG || type == OPEN_GREEN_TUBE || type == CLOSED_GREEN_TUBE;
        } else if (currentPlayer == PLAYER_1) {
            return type == YELLOW_PEG || type == OPEN_YELLOW_TUBE || type == CLOSED_YELLOW_TUBE;
        }

        return false;
    }

    private boolean isPlacementValid(byte type, int row, int col, boolean isTransfer) {
        if (isTransfer && ((board[row][col] == 0 || decodeTube(board[row][col]) == OPEN_GREEN_TUBE
                || decodeTube(board[row][col]) == OPEN_YELLOW_TUBE || decodeTube(board[row][col]) == NONE))) {
            return true;
        }
        // Check if the placement is valid based on the piece type
        System.out.println("Checking placement for type: " + type);
        switch (type) {
            case GREEN_PEG:
                // check if the peg is placed in empty cell or in a cell where a open tube is
                // placed
                return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE
                        && (board[row][col] == 0 || board[row][col] == OPEN_YELLOW_TUBE
                                || board[row][col] == OPEN_GREEN_TUBE);
            case YELLOW_PEG:
                // check if the peg is placed in empty cell or in a cell where a open tube is
                // placed
                return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE
                        && (board[row][col] == 0 || decodeTube(board[row][col]) == OPEN_GREEN_TUBE
                                || decodeTube(type) == OPEN_YELLOW_TUBE);
            case OPEN_GREEN_TUBE:
                // check if the tube is placed in empty cell or in a cell where a peg is placed
                return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE
                        && (board[row][col] == 0 || decodePeg(board[row][col]) == GREEN_PEG
                                || decodePeg(board[row][col]) == YELLOW_PEG);
            case CLOSED_GREEN_TUBE:
                return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == 0;
            case OPEN_YELLOW_TUBE:
                return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE
                        && (board[row][col] == 0 || decodePeg(board[row][col]) == YELLOW_PEG
                                || decodePeg(board[row][col]) == GREEN_PEG);
            case CLOSED_YELLOW_TUBE:
                return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == 0;
            default:
                return false;
        }
    }

    private boolean isCompatible(byte sourcePiece, byte destPiece, boolean isTransfer) {
        int sourcePeg = decodePeg(sourcePiece);
        int sourceTube = decodeTube(sourcePiece);
        int destPeg = decodePeg(destPiece);
        int destTube = decodeTube(destPiece);

        if (!isTransfer || destPiece != OPEN_GREEN_TUBE || destPiece != OPEN_YELLOW_TUBE
                || destPiece != NONE) {
            return false; // If not a transfer, destination must be empty unless specific
            // interactions are
            // allowed
        }
        // Check compatibility of placing or transferring a piece into another
        switch (sourcePeg) {
            case GREEN_PEG:
                // Green peg can be placed in an open tube or empty space
                return destTube == OPEN_YELLOW_TUBE || destTube == NONE || destTube == OPEN_GREEN_TUBE;
            case YELLOW_PEG:
                // Yellow peg can be placed in an open tube or empty space
                return destTube == OPEN_GREEN_TUBE || destTube == NONE || destTube == OPEN_YELLOW_TUBE;
        }

        // Additional checks if it's a tube interaction
        // closed Tubes cannot be placed where a peg exists
        if (sourceTube == CLOSED_GREEN_TUBE || sourceTube == CLOSED_YELLOW_TUBE) {
            return destPeg == NONE;
        }
        return true;
    }

    @Override
    public int getValue() {
        System.out.println("Evaluating board value");
        int greenScore = 0;
        int yellowScore = 0;
        for (Group group : activeGroups) {
            System.out.println("Group (getValue): " + group);
            if (group.owner == PLAYER_0) {
                System.out.println("Green Group (getvals): " + group);
                greenScore += group.evaluateScore();
            } else {
                System.out.println("Yellow Group (getvals): " + group);
                yellowScore += group.evaluateScore();
            }
        }
        return greenScore - yellowScore;
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
                        validMoves.add(new Peg5Move(pieceType, new Position(row, col), false, null));
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
            return; // Nothing to undo if the history is empty
        }

        Peg5Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Retrieve the piece types from the encoded board state
        byte pieceAtDest = board[lastMove.position.row][lastMove.position.col];
        byte pegAtDest = decodePeg(pieceAtDest);
        byte tubeAtDest = decodeTube(pieceAtDest);

        if (lastMove.isTransfer) {
            // TODO: Check if the transfer is valid, revist this logic
            if (pegAtDest != NONE && tubeAtDest != NONE) {
                // If the destination has both a peg and a tube, we need to clear the tube
                board[lastMove.position.row][lastMove.position.col] = encodePiece(pegAtDest, NONE);
            } else {
                // Clear the destination position since we are transferring the piece
                board[lastMove.position.row][lastMove.position.col] = 0;
            }
        } else {
            // If it was not a transfer, just clear the destination
            board[lastMove.position.row][lastMove.position.col] = 0;
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
        currentPlayer = -currentPlayer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Append header for the board, showing unplayed pieces or any preliminary
        // information
        sb.append("Green Unplayed: Pegs=").append(greenUnplayedPegs[0]).append(", Open Tubes=")
                .append(greenUnplayedOpenTubes[0]).append(", Closed Tubes=").append(greenUnplayedClosedTubes[0])
                .append("\n");
        sb.append("Yellow Unplayed: Pegs=").append(yellowUnplayedPegs[0]).append(", Open Tubes=")
                .append(yellowUnplayedOpenTubes[0]).append(", Closed Tubes=").append(yellowUnplayedClosedTubes[0])
                .append("\n");

        // Append the actual board with row and column labels
        sb.append("  "); // Leading space for row numbers
        for (int i = 0; i < BOARD_SIZE; i++) {
            sb.append(String.format("%3d", i + 1)); // Print column numbers
        }
        sb.append("\n");

        for (int row = 0; row < BOARD_SIZE; row++) {
            sb.append(String.format("%2d", row + 1)); // Print row number
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(String.format("%3s", pieceToString(board[row][col]))); // Convert each piece code to a string
            }
            sb.append("\n");
        }

        // Append footer with current game status or additional info
        sb.append("Current Player: ").append(currentPlayer == PLAYER_0 ? "Green" : "Yellow").append(" to play\n");
        sb.append(groupManager.toString()).append("\n");
        sb.append("Active Groups: ");
        for (Group group : activeGroups) {
            sb.append(group.toString()).append(", ");
        }
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

    // public static void main(String[] args) {
    // // Set up the board instance to test
    // Peg5Board board = new Peg5Board();
    // board.currentPlayer = PLAYER_0; // Start with player 0

    // // Print the board before any moves
    // System.out.println("Initial Board:");
    // System.out.println(board);

    // // Test placement of each type of peg and tube on the board
    // byte[] types = { GREEN_PEG, YELLOW_PEG, OPEN_GREEN_TUBE, CLOSED_GREEN_TUBE,
    // OPEN_YELLOW_TUBE,
    // CLOSED_YELLOW_TUBE };
    // int testRow = 0, testCol = 0;

    // for (byte type : types) {
    // // Create and apply move
    // Peg5Move testMove = board.new Peg5Move(type, new Position(testRow, testCol),
    // false, null);
    // try {
    // board.applyMove(testMove);
    // System.out.println("After placing " + testMove.getTypeName() + " at (" +
    // (testRow + 1) + ","
    // + (testCol + 1) + "):");
    // System.out.println(board);
    // } catch (InvalidMoveException e) {
    // System.out.println("Failed to place " + testMove.getTypeName() + " at (" +
    // (testRow + 1) + ","
    // + (testCol + 1) + "): " + e.getMessage());
    // }

    // // Prepare next test position
    // testCol++;
    // if (testCol >= BOARD_SIZE) {
    // testCol = 0;
    // testRow++;
    // }
    // }

    // // Test transfers if applicable
    // // Assuming we want to test a transfer from the first position to the last
    // if (BOARD_SIZE > 1) { // Ensure the board is large enough
    // byte type = GREEN_PEG; // Change as needed
    // Peg5Move transferMove = board.new Peg5Move(type, new Position(BOARD_SIZE - 1,
    // BOARD_SIZE - 1), true,
    // new Position(0, 0));
    // try {
    // board.applyMove(transferMove);
    // System.out.println("After transferring " + transferMove.getTypeName() + " to
    // (" + BOARD_SIZE + ","
    // + BOARD_SIZE + "):");
    // System.out.println(board);
    // } catch (InvalidMoveException e) {
    // System.out.println("Failed to transfer " + transferMove.getTypeName() + ": "
    // + e.getMessage());
    // }
    // }
    // }
}
