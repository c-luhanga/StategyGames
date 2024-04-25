package edu.principia.charles.OODesign.StrategyGames.Peg5;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private List<Position> positions;

    public Group(int startRow, int startCol, int rowInc, int colInc) {
        this.positions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            positions.add(new Position(startRow + i * rowInc, startCol + i * colInc, (byte) 0)); // Initialize with no
                                                                                                 // piece
        }
    }

    public void updatePosition(Position updatedPosition, byte pieceType) {
        for (Position pos : positions) {
            if (pos.row == updatedPosition.row && pos.col == updatedPosition.col) {
                pos.updatePieceType(pieceType);
                break; // Once found, no need to continue
            }
        }
    }

    public void undoLastMove() {
        for (Position position : positions) {
            position.undoUpdate();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Group: ");
        for (Position pos : positions) {
            sb.append(pos.toString()).append(" ");
        }
        return sb.toString();
    }

    public byte[] getPositionsAsLine() {
        byte[] line = new byte[5];
        for (int i = 0; i < 5; i++) {
            line[i] = positions.get(i).pieceType;
        }
        return line;
    }
}
