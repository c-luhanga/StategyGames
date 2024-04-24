package edu.principia.charles.OODesign.StrategyGames.TicTacToe;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.principia.OODesign.StrategyGames.Board.Board;

public class TicTacToeBoard implements Board, java.io.Serializable {
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
        int[] rowSum = new int[SIZE];
        int[] colSum = new int[SIZE];
        int diagSum = 0;
        int antiDiagSum = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                rowSum[row] += board[row][col];
                colSum[col] += board[row][col];
                if (row == col) {
                    diagSum += board[row][col];
                }
                if (row + col == SIZE - 1) {
                    antiDiagSum += board[row][col];
                }
            }
        }
        // check for a win
        if (Math.abs(diagSum) == SIZE || Math.abs(antiDiagSum) == SIZE) {
            return diagSum == SIZE ? WIN : -WIN;
        }
        for (int i = 0; i < SIZE; i++) {
            if (Math.abs(rowSum[i]) == SIZE || Math.abs(colSum[i]) == SIZE) {
                return rowSum[i] == SIZE ? WIN : -WIN;
            }
        }
        // use moveHistory to determine if the game is a draw
        if (moveHistory.size() == SIZE * SIZE) {
            return 0;
        }
        // check for 2 in a row and return value based on the number of them
        int value = 0;
        for (int i = 0; i < SIZE; i++) {

            if (rowSum[i] == 2) {
                value++;
            }
            if (rowSum[i] == -2) {
                value--;
            }
            if (colSum[i] == 2) {
                value++;
            }
            if (colSum[i] == -2) {
                value--;
            }
        }
        if (diagSum == 2) {
            value++;
        }
        if (diagSum == -2) {
            value--;
        }
        if (antiDiagSum == 2) {
            value++;
        }
        if (antiDiagSum == -2) {
            value--;
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

        // Board cells
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

        // Current player
        sb.append("\nCurrent player: ");
        sb.append(currentPlayer == PLAYER_X ? "X" : "O");

        // Winner
        int gameValue = getValue();
        if (gameValue == 1000000) {
            sb.append("\nWinner: X");
        } else if (gameValue == -1000000) {
            sb.append("\nWinner: O");
        } else if (gameValue == 0) {
            sb.append("\nDraw");
        }

        return sb.toString();
    }

    // Inner class representing a Tic Tac Toe move

}
