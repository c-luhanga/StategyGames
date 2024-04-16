package BoardGames.Peg5;

public class Position {
    private int row;
    private int col;
    private int player; // Might use -1, 0, 1 for no player, player1, player2 etc.
    private byte piece; // This could be an enum or byte depending on how pieces are defined

    // Constructor
    public Position(int row, int col, int player, int board) {
        this.row = row;
        this.col = col;
        this.player = player;
        this.piece = board;
    }

    // Getters
    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    public int getPlayer() {
        return player;
    }

    public byte getPiece() {
        return piece;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
