package BoardGames.TicTacToe;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BoardGames.Board_Interface.Board;
import BoardGames.Board_Interface.Board.InvalidMoveException;
import BoardGames.Board_Interface.Board.Move;

public class TicTacToeBoard implements Board {
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

        public void write(OutputStream os) throws java.io.IOException {

            os.write(((row + 1) << 4) | (col + 1));
        }
        // read the move as a single byte row and column in the high and low bits

        public void read(InputStream is) throws java.io.IOException {

            int b = is.read();

            row = (b >> 4) - 1;
            col = (b & 0x0F) - 1;

        }

        public void fromString(String s) throws java.io.IOException {
            String[] parts = s.split(",");
            if (parts.length != 2) {
                throw new java.io.IOException("Invalid move string");
            }
            try {
                row = Integer.parseInt(parts[0]) - 1;
                col = Integer.parseInt(parts[1]) - 1;
                if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
                    throw new java.io.IOException("Invalid move string");
            } catch (NumberFormatException e) {
                throw new java.io.IOException("Invalid move string");
            }

        }

        public int compareTo(Board.Move m) {

            TicTacToeMove other = (TicTacToeMove) m;

            return row == other.row ? col - other.col : row - other.row;

        }

        public String toString() {
            return (row + 1) + "," + (col + 1);
        }

    }

    private int[][] board;
    private int currentPlayer;
    private List<TicTacToeMove> moveHistory;
    private TicTacToeBoard.TicTacToeMove currentMove;

    private static final int SIZE = 3;
    private static final int EMPTY = 0;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_O = -1;

    public TicTacToeBoard() {
        board = new int[SIZE][SIZE];
        currentPlayer = PLAYER_X;
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
            throw new InvalidMoveException("Invalid move");
        }
        if (board[move.row][move.col] != EMPTY) {
            throw new InvalidMoveException("Cell is already occupied");
        }
        board[move.row][move.col] = currentPlayer;
        currentPlayer = -currentPlayer;
        moveHistory.add(new TicTacToeMove(move.row + 1, move.col + 1));
    }

    @Override
    public int getValue() {
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
        int value = 0;
        for (int i = 0; i < SIZE; i++) {
            int rowSum = 0;
            int colSum = 0;
            for (int j = 0; j < SIZE; j++) {
                rowSum += board[i][j];
                colSum += board[j][i];
            }
            if (rowSum == SIZE || colSum == SIZE) {
                return WIN;
            } else if (rowSum == -SIZE || colSum == -SIZE) {
                return -WIN;
            }
            value += rowSum + colSum;
        }
        int diag1 = 0;
        int diag2 = 0;
        for (int i = 0; i < SIZE; i++) {
            diag1 += board[i][i];
            diag2 += board[i][SIZE - i - 1];
        }
        if (diag1 == SIZE || diag2 == SIZE) {
            return WIN;
        } else if (diag1 == -SIZE || diag2 == -SIZE) {
            return -WIN;
        }
        value += diag1 + diag2;
        if (value == 0) {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == EMPTY) {
                        return 0;
                    }
                }
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
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == PLAYER_X) {
                    sb.append('X');
                } else if (board[i][j] == PLAYER_O) {
                    sb.append('O');
                } else {
                    sb.append(' ');
                }
                if (j < SIZE - 1) {
                    sb.append(" | ");
                }
            }
            if (i < SIZE - 1) {
                sb.append("\n---------\n");
            }
        }
        return sb.toString();
    }

    // Inner class representing a Tic Tac Toe move
    
}
