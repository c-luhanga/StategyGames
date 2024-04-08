package BoardGames;

import java.util.List;

import javax.swing.border.Border;

public class AiSolver {

    public static class mmResult {
        public int Values; // The value of the move
        public Board.Move currentMove; // The move itself
    }

    static void minimax(Board brd, int min, int max, int level, mmResult best) {
        List<? extends Board.Move> moves = brd.getValidMoves();
        if (level == 0 || brd.getValidMoves().isEmpty()) {
            best.Values = brd.getValue();
            best.currentMove = null;
            return;
        }

        if (brd.getCurrentPlayer() == 1) {
            best.Values = min;
            for (Board.Move move : moves) {
                try {
                    brd.applyMove(move);
                } catch (Exception e) {
                    assert false;
                }
                mmResult result = new mmResult();
                minimax(brd, min, result.Values, level - 1, result);
                if (result.Values > best.Values) {
                    best.Values = result.Values;
                    best.currentMove = move;
                }

                brd.undoMove();
                if (result.Values >= max) {
                    return;
                }

            }
        } else {
            best.Values = max;
            for (Board.Move move : moves) {
                try {
                    brd.applyMove(move);
                } catch (Exception e) {
                    assert false;
                }
                mmResult result = new mmResult();
                minimax(brd, min, max, level - 1, result);
                if (result.Values < best.Values) {
                    best.Values = result.Values;
                    best.currentMove = move;
                }
                brd.undoMove();
                if (result.Values <= min) {
                    return;
                }

            }
        }

    }
}