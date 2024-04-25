package edu.principia.charles.OODesign.StrategyGames.Peg5;

import java.util.Stack;

public class Position {
    public int row;
    public int col;
    public byte pieceType; // This could be an enum or byte depending on how pieces are defined
    private Stack<Byte> history; // Stack to keep track of history for undo functionality

    // Constructor for position with piece type
    public Position(int row, int col, byte i) {
        this.row = row;
        this.col = col;
        this.pieceType = i;
        this.history = new Stack<>();
    }

    // Update piece type for this position
    public void updatePieceType(byte newPieceType) {
        history.push(pieceType); // Save current state before updating
        this.pieceType = newPieceType;
    }

    public void undoUpdate() {
        if (!history.isEmpty()) {
            this.pieceType = history.pop(); // Revert to the last saved state
        }
    }

    @Override
    public String toString() {
        return String.format("Position(%d, %d) Type: %d", row, col, pieceType);
    }
}
