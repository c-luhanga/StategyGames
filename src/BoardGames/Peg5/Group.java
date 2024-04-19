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
        if (position.owner != owner) {
            throw new IllegalArgumentException("Position owner does not match group owner.");
        }
        positions.add(position);
    }

    // Check if this group forms a winning combination
    public boolean checkForWin() {
        // Require exactly 5 pieces to potentially form a winning pattern.
        if (positions.size() != 5) {
            return false;
        }

        // Convert positions to a byte array using a traditional loop.
        byte[] line = new byte[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            line[i] = positions.get(i).getPiece();
        }
        // Check if the collected line is a winning combination.
        return WinPatterns.isWinningLine(line);
    }

    public int evaluateScore() {
        // Scoring calculations require at least 3 pieces.
        if (positions.size() < 3) {
            return 0;
        }

        // Convert positions to a byte array similarly as in checkForWin.
        byte[] line = new byte[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            line[i] = positions.get(i).getPiece();
        }
        // Calculate the score based on the current line configuration.
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
