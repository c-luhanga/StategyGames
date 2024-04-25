package edu.principia.charles.OODesign.StrategyGames.Peg5;

import java.util.ArrayList;
import java.util.List;

import edu.principia.charles.OODesign.StrategyGames.Peg5.Peg5Board.Peg5Move;

public class GroupManager {
    private Peg5Board board;
    public List<Group> groups;

    public GroupManager(Peg5Board board) {
        this.board = board;
        this.groups = new ArrayList<>();
        initializeAllGroups();
    }

    private void initializeAllGroups() {
        // Horizontal and vertical groups
        for (int i = 0; i < Peg5Board.BOARD_SIZE; i++) {
            // Horizontal groups (left to right)
            for (int col = 0; col <= Peg5Board.BOARD_SIZE - 5; col++) {
                groups.add(new Group(i, col, 0, 1));
            }
            // Vertical groups (top to bottom)
            for (int row = 0; row <= Peg5Board.BOARD_SIZE - 5; row++) {
                groups.add(new Group(row, i, 1, 0));
            }
        }

        // Diagonal groups (both major and minor)
        for (int row = 0; row <= Peg5Board.BOARD_SIZE - 5; row++) {
            for (int col = 0; col <= Peg5Board.BOARD_SIZE - 5; col++) {
                // Major diagonal (top-left to bottom-right)
                groups.add(new Group(row, col, 1, 1));
                // Minor diagonal (bottom-left to top-right), ensure diagonal can fully fit
                if (col >= 4) {
                    groups.add(new Group(row, col, 1, -1));
                }
            }
        }
    }

    public void updateGroupsAfterMove(Position position, byte pieceType) {
        // Update the groups in response to a new move
        for (Group group : groups) {
            group.updatePosition(position, pieceType);
        }
    }

    public void undoLastMove(Peg5Move lastMove) {
        for (Group group : groups) {
            group.undoLastMove();
        }
    }

    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GroupManager(Active Groups: ");
        for (Group group : groups) {
            sb.append(group.toString()).append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
