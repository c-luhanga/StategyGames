package edu.principia.charles.OODesign.StrategyGames.Peg5;

public class Position {
    public int row;
    public int col;
    public int owner; // Might use -1, 0, 1 for no player, player1, player2 etc.
    public byte pieceType; // This could be an enum or byte depending on how pieces are defined

    // Constructor
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
        this.pieceType = 0; // Default to no piece
        this.owner = 0; // Default to no owner
    }

    public Position(int row, int col, byte pieceType, int owner) {
        this.row = row;
        this.col = col;
        this.pieceType = pieceType;
        this.owner = owner;
    }

    // Getters
    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    public int getPlayer() {
        return owner;
    }

    public byte getPiece() {
        return pieceType;
    }

    @Override
    public String toString() {
        return String.format("Position(%d, %d) Type: %d Owner: %d", row, col, pieceType, owner);
    }
}
