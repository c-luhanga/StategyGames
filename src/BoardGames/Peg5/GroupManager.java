package BoardGames.Peg5;

import java.util.ArrayList;
import java.util.List;

import BoardGames.Peg5.Peg5Board.Peg5Move;

public class GroupManager {
    private Peg5Board board;
    private List<Group> activeGroups;

    public GroupManager(Peg5Board board) {
        this.board = board;
        this.activeGroups = new ArrayList<>();
    }

    public void updateGroupsAfterMove(Peg5Move move) {
        // Clear existing groups and find new groups after a move
        activeGroups.clear();
        findAllGroups();
        handleMove(move);
    }

    private void findAllGroups() {
        // Scan all rows, columns, and diagonals to find and evaluate groups
        for (int row = 0; row < Peg5Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Peg5Board.BOARD_SIZE; col++) {
                // Extract groups starting from each position on the board
                extractGroupsFromPosition(row, col);
            }
        }
    }

    private void extractGroupsFromPosition(int startRow, int startCol) {
        // Directions for horizontal, vertical, and diagonal checks
        int[][] directions = {
                { 0, 1 }, // Right (Horizontal)
                { 1, 0 }, // Down (Vertical)
                { 1, 1 }, // Down-Right (Major Diagonal)
                { 1, -1 } // Down-Left (Minor Diagonal)
        };

        for (int[] dir : directions) {
            extractGroupInDirection(startRow, startCol, dir[0], dir[1]);
        }
    }

    private void extractGroupInDirection(int startRow, int startCol, int dRow, int dCol) {
        int endRow = startRow + 4 * dRow;
        int endCol = startCol + 4 * dCol;

        if (endRow >= 0 && endRow < Peg5Board.BOARD_SIZE && endCol >= 0 && endCol < Peg5Board.BOARD_SIZE) {
            List<Position> groupPositions = new ArrayList<>();
            boolean validGroup = true;
            for (int i = 0; i < 5; i++) {
                int row = startRow + i * dRow;
                int col = startCol + i * dCol;
                byte pieceType = board.board[row][col];
                if (pieceType != 0) {
                    groupPositions.add(new Position(row, col, pieceType, board.getCurrentPlayer()));
                } else {
                    validGroup = false;
                    break;
                }
            }

            if (validGroup && groupPositions.size() == 5) {
                Group newGroup = new Group(board.getCurrentPlayer());
                for (Position pos : groupPositions) {
                    newGroup.addPosition(pos);
                }
                if (newGroup.checkForWin()) {
                    activeGroups.add(newGroup);
                }
            }
        }
    }

    private void handleMove(Peg5Move move) {
        // Optionally handle additional logic if needed for moving pieces
        // (splitting/merging groups)
    }

    public List<Group> getActiveGroups() {
        return activeGroups;
    }
}
