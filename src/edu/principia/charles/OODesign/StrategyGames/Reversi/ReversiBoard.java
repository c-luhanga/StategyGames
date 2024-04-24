package edu.principia.charles.OODesign.StrategyGames.Reversi;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.principia.OODesign.StrategyGames.Board.Board;

public class ReversiBoard implements Board, Serializable {
    // Class representing row/col pair, either a location or a direction
    static public class Location implements Serializable {
        public int row;
        public int col;

        public Location(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    // class representing a number of flipped pieces, and the direction of the flip
    static private class Flip implements Serializable {
        public Location dir;
        public static int count;

        public Flip(Location dir, int count) {
            this.dir = dir;
            this.count = count;
        }
    }

    // Class representing a move, which is a location and a list of flips, initially
    // empty
    // but filled in by applyMove. represent a special "pass" move by a (-1,-1)
    // location.
    public class ReversiMove implements Move, Serializable {
        public Location loc;
        public List<Flip> flips;

        public ReversiMove(Location loc) {
            this.loc = loc;
            this.flips = new ArrayList<Flip>();
        }

        @Override
        public void write(OutputStream os) throws java.io.IOException {
            os.write(loc.row);
            os.write(loc.col);
            os.write(flips.size());
            for (Flip f : flips) {
                os.write(f.dir.row);
                os.write(f.dir.col);
                os.write(f.count);
            }
        }

        @Override
        public void read(InputStream is) throws java.io.IOException {
            loc.row = is.read() << 24 >> 24;
            loc.col = is.read() << 24 >> 24;
            int flipcount = is.read();
            flips.clear();
            for (int i = 0; i < flipcount; i++) {
                Location dir = new Location(is.read(), is.read());
                int count = is.read();
                flips.add(new Flip(dir, count));
            }

        }

        @Override
        // return either row,col for a non-pass move, or "pass" for a pass move.
        // do not return flips.
        public String toString() {
            if (loc.row == -1) {
                return "pass";
            }
            return (loc.row + 1) + "," + (loc.col + 1);
        }

        @Override
        public void fromString(String s) {
            if (s.equals("pass")) {
                loc.row = -1;
                loc.col = -1;
            } else {
                String[] parts = s.split(",");
                loc.row = Integer.parseInt(parts[0]) - 1;
                loc.col = Integer.parseInt(parts[1]) - 1;
            }
            flips.clear();
        }

        @Override
        public int compareTo(Move o) {
            ReversiMove other = (ReversiMove) o;

            return loc.row != other.loc.row ? loc.row - other.loc.row : loc.col - other.loc.col;

        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ReversiMove) {
                ReversiMove other = (ReversiMove) o;
                return loc.row == other.loc.row && loc.col == other.loc.col;
            }
            return false;
        }
    }

    // static 8x8 array of weights for each board position, used in getValue,
    // corners have weight 5; edges have weight 3; other positions have weight 1,
    // except for cells one away from edges, which have weight -1. (this includes
    // edge cells
    // that are one-away from corners.)
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

    // Array of Locations representing the 8 directions on the board clockwise from
    // north.
    private static final Location[] DIRECTIONS = {
            new Location(-1, 0), new Location(-1, 1), new Location(0, 1), new Location(1, 1),
            new Location(1, 0), new Location(1, -1), new Location(0, -1), new Location(-1, -1)
    };

    private byte[][] board;

    // history of all moves thus far made.
    private List<ReversiMove> moveHistory;

    // current player, either PLAYER_0 or PLAYER_1
    private int currentPlayer;

    // number of sequential passes. (2 indicates game over)
    private int passCount;

    // dimensions of the board
    private static final int DIM = 8;

    public ReversiBoard() {
        board = new byte[DIM][DIM];
        moveHistory = new ArrayList<ReversiMove>();
        currentPlayer = PLAYER_0;
        passCount = 0;
        int center = DIM / 2;
        board[center - 1][center - 1] = PLAYER_1;
        board[center][center] = PLAYER_1;
        board[center - 1][center] = PLAYER_0;
        board[center][center - 1] = PLAYER_0;
    }

    @Override
    public Move createMove() {
        return new ReversiMove(new Location(-1, -1));
    }

    // utility to check that row/col are in bounds
    private final boolean inBounds(int row, int col) {
        return row >= 0 && row < DIM && col >= 0 && col < DIM;
    }

    @Override
    public void applyMove(Move m) throws InvalidMoveException {
        ReversiMove move = (ReversiMove) m;
        if (move.loc.row == -1) {
            passCount++;
        } else if (board[move.loc.row][move.loc.col] != 0) {
            throw new InvalidMoveException("Invalid move");
        } else {
            for (Location dir : DIRECTIONS) {
                int row = move.loc.row + dir.row;
                int col = move.loc.col + dir.col;
                int count = 0;
                while (inBounds(row, col) && board[row][col] == -currentPlayer) {
                    row += dir.row;
                    col += dir.col;
                    count++;
                }
                if (count > 0 && inBounds(row, col) && board[row][col] == currentPlayer) {
                    for (int i = 1; i <= count; i++) {
                        board[move.loc.row + i * dir.row][move.loc.col + i * dir.col] = (byte) currentPlayer;
                    }
                    move.flips.add(new Flip(dir, count));
                }
            }
            if (move.flips.size() == 0) {
                throw new InvalidMoveException("Invalid move");
            }
            board[move.loc.row][move.loc.col] = (byte) currentPlayer;
            passCount = 0;
        }
        moveHistory.add(move);
        currentPlayer = -currentPlayer;
    }

    @Override
    public List<? extends Move> getValidMoves() {
        List<ReversiMove> validMoves = new ArrayList<ReversiMove>();
        if (passCount != 2) {
            for (int row = 0; row < DIM; row++) {
                for (int col = 0; col < DIM; col++) {
                    if (board[row][col] == 0) {
                        for (Location dir : DIRECTIONS) {
                            int count = 0;
                            int r = row + dir.row;
                            int c = col + dir.col;
                            while (inBounds(r, c) && board[r][c] == -currentPlayer) {
                                r += dir.row;
                                c += dir.col;
                                count++;
                            }
                            if (count > 0 && inBounds(r, c) && board[r][c] == currentPlayer) {
                                validMoves.add(new ReversiMove(new Location(row, col)));
                                break;
                            }
                        }
                    }
                }
            }
            if (validMoves.size() == 0) {
                validMoves.add(new ReversiMove(new Location(-1, -1)));
            }
        }
        return validMoves;
    }

    @Override
    public List<? extends Move> getMoveHistory() {
        return moveHistory;
    }

    @Override
    public int getValue() {
        int value = 0;
        // If game is over, assign WIN, -WIN or 0 depending on which
        // player has the most squares regardless of weight. do this by
        // just totalling the +1 and -1 squares.
        if (passCount == 2) {
            for (int row = 0; row < DIM; row++) {
                for (int col = 0; col < DIM; col++) {
                    value += board[row][col];
                }
            }
            return value > 0 ? WIN : value < 0 ? -WIN : 0;
        }

        for (int row = 0; row < DIM; row++) {
            for (int col = 0; col < DIM; col++) {
                value += board[row][col] * WEIGHTS[row][col];
            }
        }
        return value;
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public void undoMove() {
        if (moveHistory.size() > 0) {
            ReversiMove move = moveHistory.remove(moveHistory.size() - 1);
            currentPlayer = -currentPlayer;
            if (move.loc.row != -1) { // if not a pass move
                // undoing a nonpass move that followed a pass move will not properly
                // restore a prior nonzero passcount. but this is okay since in this situation
                // passcount cannot reach 2, so an inaccurate passcount will not affect
                // the game.
                board[move.loc.row][move.loc.col] = 0;
                for (Flip f : move.flips) {
                    for (int i = 1; i <= f.count; i++) {
                        int newRow = move.loc.row + i * f.dir.row;
                        int newCol = move.loc.col + i * f.dir.col;
                        if (newRow >= 0 && newRow < board.length && newCol >= 0 && newCol < board[0].length) {
                            board[newRow][newCol] = (byte) -currentPlayer;
                        }
                    }
                }
            } else {
                passCount--;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // append column headers
        sb.append("  1 2 3 4 5 6 7 8\n");
        for (int row = 0; row < DIM; row++) {
            sb.append(row + 1 + " ");
            for (int col = 0; col < DIM; col++) {
                sb.append(board[row][col] == PLAYER_0 ? "X " : board[row][col] == PLAYER_1 ? "O " : ". ");
            }
            sb.append("\n");
        }
        sb.append("Current player is " + (currentPlayer == PLAYER_0 ? "X" : "O") + "\n");
        return sb.toString();
    }
}