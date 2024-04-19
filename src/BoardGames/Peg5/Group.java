package BoardGames.Peg5;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Group {
    // List of positions that make up this group
    public List<Position> positions;

    // Owner of the group, assuming a group can only belong to one player
    public int owner;

    // Constructor
    public Group(int owner) {
        this.positions = new ArrayList<>();
        this.owner = owner;
    }

    // Adds a position to the group
    public void addPosition(Position position) {
        positions.add(position);
    }

    // Check if this group forms a winning combination
    public boolean checkForWin() {
        if (positions.size() < 5) {
            return false; // A winning group must have exactly 5 pieces
        }
        byte[] line = new byte[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            line[i] = positions.get(i).pieceType;
        }
        return WinPatterns.isWinningLine(line);
    }

    public int evaluateScore() {
        if (positions.size() < 3) {
            return 0; // Not enough pieces to form a meaningful pattern
        }
        byte[] line = new byte[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            line[i] = positions.get(i).pieceType;
        }
        return WinPatterns.evaluateLineScore(line, (byte) owner);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Group(Owner: " + owner + " Positions: ");
        for (Position position : positions) {
            sb.append(position.toString()).append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
