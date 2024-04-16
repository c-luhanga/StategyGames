package BoardGames.AiSolver;

import java.util.List;

import javax.swing.border.Border;

import BoardGames.Board_Interface.Board;
import BoardGames.Board_Interface.Board.Move;

public class AiSolver {

    public static class mmResult {
        public int Values; // The value of the move
        public Board.Move currentMove; // The move itself
    }

    public static void minimax(Board brd, int min, int max, int level, mmResult best) {
        List<? extends Board.Move> moves = brd.getValidMoves();
        System.out.printf("MM: (%d, %d) lvl: %d\n", min, max, level);

        if (level == 0 || moves.isEmpty()) {
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
                minimax(brd, best.Values, max, level - 1, result);
                if (result.Values > best.Values) {
                    best.Values = result.Values;
                    best.currentMove = move;
                }

                brd.undoMove();
                if (best.Values >= max) {
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
                minimax(brd, min, best.Values, level - 1, result);
                if (result.Values < best.Values) {
                    best.Values = result.Values;
                    best.currentMove = move;
                }
                brd.undoMove();
                if (best.Values <= min) {
                    return;
                }

            }
        }

    }
}