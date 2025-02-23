package boardgames.edu.principia.csci240.strategygames.cluhanga.tictactoe;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import boardgames.edu.principia.csci240.strategygames.Board;

public class TTTBoard implements Board {
    public class TicTacToeMove implements Move, java.io.Serializable {
        public int row;
        public int col;

        public TicTacToeMove() {
            row = -1;
            col = -1;
        }

        public TicTacToeMove(int row, int col) {

            this.row = row - 1;

            this.col = col - 1;
        }

        // write the move as a single byte row and column in the high and low bits
        @Override
        public void write(OutputStream os) throws java.io.IOException {

            os.write(((row + 1) << 4) | (col + 1));
        }

        // read the move as a single byte row and column in the high and low bits
        @Override
        public void read(InputStream is) throws java.io.IOException {

            int b = is.read();

            row = (b >> 4) - 1;
            col = (b & 0x0F) - 1;

        }

        @Override
        public void fromString(String s) throws java.io.IOException {
            String[] parts = s.split(",");
            if (parts.length != 2) {
                throw new java.io.IOException("Invalid move format: " + s);
            }
            try {
                row = Integer.parseInt(parts[0]) - 1;
                col = Integer.parseInt(parts[1]) - 1;
            } catch (NumberFormatException e) {
                throw new java.io.IOException("Invalid number format: " + s);
            }

        }

        @Override
        public int compareTo(Board.Move m) {
            TicTacToeMove other = (TicTacToeMove) m;
            return row == other.row ? col - other.col : row - other.row;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            TicTacToeMove other = (TicTacToeMove) obj;
            return row == other.row && col == other.col;
        }

        @Override
        public String toString() {
            return (row + 1) + "," + (col + 1);
        }

    }

    private int[][] board;
    private int currentPlayer; // PLAYER_X or PLAYER_O
    private List<TicTacToeMove> moveHistory;
    private static final int SIZE = 3;
    private static final int EMPTY = 0;

    public TTTBoard() {
        board = new int[SIZE][SIZE];
        currentPlayer = PLAYER_1;
        moveHistory = new ArrayList<>();
    }

    @Override
    public Move createMove() {
        return new TicTacToeMove();
    }

    @Override
    public void applyMove(Move m) throws InvalidMoveException {
        TicTacToeMove move = (TicTacToeMove) m;
        if (move.row < 0 || move.row >= SIZE || move.col < 0 || move.col >= SIZE) {
            throw new InvalidMoveException("Invalid move! Out of bounds");
        }
        if (board[move.row][move.col] != EMPTY) {
            throw new InvalidMoveException("Cell is already occupied");
        }
        board[move.row][move.col] = currentPlayer;
        currentPlayer *= -1;
        moveHistory.add(new TicTacToeMove(move.row + 1, move.col + 1));
    }

    // Return the current state of the game
    // given a board, the value should be calcula

    // Implement game logic to return the current state of the game
    // Check for wins, losses, and draws, return appropriate value
    // Use the WIN constant for a win condition
    // return getVaule should return who is currently winning or if it is a draw
    // check who is closer to winning and return the value
    // players that have two in a row or column or diagonal are closer to winning
    // regardless
    // of if they are right next to each other or not
    // 0 for draw, 1 for player 0, -1 for player 1
    // 1000000 for player 0 win, -1000000 for player 1 win
    // the value is the sum of the rows, columns, and diagonals
    // for use with AI to determine best move
    // name variables properly and use constants for magic numbers
    // check for wins and draw before returning value
    @Override
    public int getValue() {
        int value = 0;
        int[] rowSum = new int[SIZE];
        int[] colSum = new int[SIZE];
        int diagSum1 = 0;
        int diagSum2 = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                rowSum[row] += board[row][col];
                colSum[col] += board[row][col];
                if (row == col) {
                    diagSum1 += board[row][col];
                }
                if (row + col == SIZE - 1) {
                    diagSum2 += board[row][col];
                }
            }
        }

        // Check for wins and losses or draw
        for (int i = 0; i < SIZE; i++) {
            if (rowSum[i] == SIZE || colSum[i] == SIZE || diagSum1 == SIZE
                    || diagSum2 == SIZE) {
                return WIN;
            } else if (rowSum[i] == -SIZE || colSum[i] == -SIZE
                    || diagSum1 == -SIZE || diagSum2 == -SIZE) {
                return -WIN;
            }
        }

        // check for draw
        if (moveHistory.size() == SIZE * SIZE) {
            return 0;
        }

        // Otherwise return the heuristic value
        for (int i = 0; i < SIZE; i++) {
            int oneWay = SIZE - 1;
            if (rowSum[i] == oneWay || colSum[i] == oneWay || diagSum1 == oneWay
                    || diagSum2 == oneWay) {
                value++;
            } else if (rowSum[i] == -oneWay || colSum[i] == -oneWay || diagSum1 == -oneWay
                    || diagSum2 == -2) {
                value--;
            }

        }
        return value;
    }

    @Override
    public List<TicTacToeMove> getValidMoves() {
        List<TicTacToeMove> validMoves = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    validMoves.add(new TicTacToeMove(row + 1, col + 1)); // Adjust for 1-based indexing
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
    public List<TicTacToeMove> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    @Override
    public void undoMove() {
        if (!moveHistory.isEmpty()) {
            TicTacToeMove lastMove = moveHistory.remove(moveHistory.size() - 1);
            int row = lastMove.row;
            int col = lastMove.col;
            if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
                board[row][col] = EMPTY;
                currentPlayer = -currentPlayer;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == PLAYER_1) {
                    sb.append('X');
                } else if (board[row][col] == PLAYER_2) {
                    sb.append('O');
                } else {
                    sb.append(' ');
                }
                if (col < SIZE - 1) {
                    sb.append(" | ");
                }
            }
            if (row < SIZE - 1) {
                sb.append("\n---------\n");
            }
        }
        return sb.toString();
    }
    // Inner class representing a Tic Tac Toe move

}
