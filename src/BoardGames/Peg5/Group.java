package BoardGames.Peg5;

import java.util.List;
import java.util.Map;

public class Group {
    // list <array> of pieces in the group
    private List<Position> pieces;
    // collection possibly a map of the pieces in the group, the map will hold
    // potential wins e.g 3 in a row or 4 in a row
    private Map<Position, Integer> pieceMap;
    // the player who owns the group
    private int player;

    public void addPiece(Position piece) {
        // add the piece to the group
        pieces.add(piece);
        // add the piece to the map
        pieceMap.put(piece, pieces.size());
    }

    public void removePiece(Position piece) {
        // remove the piece from the group
        pieces.remove(piece);
        // remove the piece from the map
        pieceMap.remove(piece);
    }

    public void potentialWins() {
        // check if the group has a potential win
        // evaluate the group to see if it has a potential win
        // maybe only check group if it has 3 or more pieces
        // if the group has a potential win, return the player who owns the group
        // else return 0

    }
}
