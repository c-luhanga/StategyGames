package BoardGames.Reversi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import BoardGames.Board_Interface.Board;

public class ReversiBoard implements BoardGames.Board_Interface.Board {
    // Class representing row/col pair
    public static class Location {
        public int row;
        public int col;

        public Location(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    // A Class representing a number of flipped pieces and the direction of the flip
    // use Location class
    public static class Flip {
        public int count;
        public Location direction;

        public Flip(int count, Location direction) {
            this.count = count;
            this.direction = direction;
        }
    }

    // Class representing a move , which is a location and a list of flips,
    // initialized to empty
    // but can be filled in by the applyMove method
    // Represnt a special "pass" move is represented by a move with an empty list of
    // flips (-1,-1)
    public class ReversiMove implements Move {
        public Location location;
        public List<Flip> flips;

        public ReversiMove(Location location) {
            this.location = location;
            this.flips = new ArrayList<Flip>();
        }

        @Override
        public void write(OutputStream os) throws IOException {
            DataOutputStream dos = new DataOutputStream(os);
            dos.write(location.row);
            dos.write(location.col);
            dos.write(flips.size());
            for (Flip flip : flips) {
                dos.write(flip.count);
                dos.write(flip.direction.row);
                dos.write(flip.direction.col);
            }
        }

        @Override
        public void read(InputStream is) throws IOException {
            location.row = is.read();
            location.col = is.read();
            int flipCount = is.read();
            flips.clear();
            for (int i = 0; i < flipCount; i++) {
                int count = is.read();
                int row = is.read();
                int col = is.read();
                flips.add(new Flip(count, new Location(row, col)));
            }
        }

        @Override
        // Implement parsing from a string representation, there is a comma separated,
        // pass is also an option as a move
        public void fromString(String s) {
            String[] parts = s.split(",");
            if (s.equals("pass")) {
                location.row = -1;
                location.col = -1;
            } else {
                location.row = Integer.parseInt(parts[0]);
                location.col = Integer.parseInt(parts[1]);
            }
            flips.clear();
        }

        @Override
        // Return either row and col for a non-pass, or pass for a move
        // do not return the flips
        public String toString() {
            if (location.row == -1) {
                return "pass";
            } else {
                return location.row + "," + location.col;
            }
        }

        @Override
        public int compareTo(Move o) {
            ReversiMove other = (ReversiMove) o;
            return location.row != other.location.row ? location.col - other.location.col
                    : location.row - other.location.row;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ReversiMove) {
                ReversiMove other = (ReversiMove) o;
                return location.row == other.location.row && location.col == other.location.col;
            }
            return false;
        }

    }

    // Static 8x8 board of weights for each square, used for evaluation in getValue,
    // Coner squares are the most valuable, followed by edge squares, and then the
    // rest
    // conners worth 5, edges worth 3, and the rest worth 1
    // except for cells that are one away from the corner, which are worth -1 (This
    // includes the edge cells
    // that are one away from the corner)
    private static final int[][] WEIGHTS = {
            { 5, -1, 3, 3, 3, 3, -1, 5 },
            { -1, -1, -1, -1, -1, -1, -1, -1 },
            { 3, -1, 1, 1, 1, 1, -1, 3 },
            { 3, -1, 1, 1, 1, 1, -1, 3 },
            { 3, -1, 1, 1, 1, 1, -1, 3 },
            { 3, -1, 1, 1, 1, 1, -1, 3 },
            { -1, -1, -1, -1, -1, -1, -1, -1 },
            { 5, -1, 3, 3, 3, 3, -1, 5 }
    };
    // Array of locations representing the 8 possible directions to flip pieces
    // clockwise
    // from north
    private static final Location[] DIRECTIONS = {
            new Location(-1, 0),
            new Location(-1, 1),
            new Location(0, 1),
            new Location(1, 1),
            new Location(1, 0),
            new Location(1, -1),
            new Location(0, -1),
            new Location(-1, -1)
    };
    // Array representation of the board with 0 for empty, 1 for player 1, -1 for
    // player 2,
    private int DIM;
    private byte[][] board;
    // history of moves
    private List<ReversiMove> moveHistory;
    // current player PLAYER_1 or PLAYER_2
    private int currentPlayer;
    // number of passes in a row
    private int passes;

    // no magic numbers
    public ReversiBoard() {
        DIM = 8;
        board = new byte[DIM][DIM];
        board[DIM / 2 - 1][DIM / 2 - 1] = 1;
        board[DIM / 2][DIM / 2] = 1;
        board[DIM / 2 - 1][DIM / 2] = -1;
        board[DIM / 2][DIM / 2 - 1] = -1;
        moveHistory = new ArrayList<>();
        currentPlayer = PLAYER_1;
        passes = 0;
    }

    @Override
    public Move createMove() {
        return new ReversiMove(new Location(-1, -1));
    }

    @Override
    public void applyMove(Move m) throws InvalidMoveException {
        ReversiMove move = (ReversiMove) m;
        if (move.location.row == -1) {
            passes++;
            if (passes == 2) {
                throw new InvalidMoveException("Two passes in a row");
            }
            currentPlayer = -currentPlayer;
            return;
        }
        if (move.location.row < 0 || move.location.row >= DIM || move.location.col < 0 || move.location.col >= DIM) {
            throw new InvalidMoveException("Invalid move");
        }
        if (!move.flips.isEmpty()) {
            board[move.location.row][move.location.col] = (byte) currentPlayer;
            for (Flip flip : move.flips) {
                Location loc = new Location(move.location.row, move.location.col);
                for (int i = 0; i < flip.count; i++) {
                    loc.row += flip.direction.row;
                    loc.col += flip.direction.col;
                    board[loc.row][loc.col] = (byte) currentPlayer;
                }
            }
            passes = 0;
            moveHistory.add(move);
            currentPlayer = -currentPlayer;
        } else {
            throw new InvalidMoveException("Invalid move");
        }
    }

}
