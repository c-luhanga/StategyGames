package edu.principia.OODesign.StrategyGames.Tournament;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import edu.principia.OODesign.StrategyGames.Board.Board;
import edu.principia.charles.OODesign.StrategyGames.AiSolver.AiSolver;

public class Tournament {
    // Result of one two-player competition, showing which player won and
    // moves made by each player
    public static class CmpResult {
        Board winner;
        Board loser;
    }

    // Run a competition between two Boards, with brd0 as player 0 and brd1 as
    // player 1.
    // use AiSolver for each move. Alternate between Boards, with board determining
    // its move using AiSolver, and the other board applying the move to track
    // together. Each
    // board initially has a starting level of initLevel, and the time allowed for
    // each move is timepermove.
    // However, if a board has takem an average time more than timepermove for its
    // moves, its level is reduced by 1.
    // (but never < 1). until its average time is less than timepermove. whereupon
    // its level is increased by 1
    // but never > initLevel. Time used is mesured in nanoseconds. via
    // ThreadBean.getCurrentThreadCpuTime().

    // the competition ends when AiPlayer returns an a null best move,
    // the return value from AiPlayer.minimax determines who has won or if it is a
    // draw.
    // Return the winner and loser of the competition.
    // as a CmpResult. if a draw return, the winner is with the lower average time
    // per move.
    public CmpResult runCompetition(Board brd0, Board brd1, int timepermove, int maxLevel) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        Board boards[] = { brd0, brd1 };
        int levels[] = { maxLevel, maxLevel };
        long totalTimes[] = { 0, 0 };
        int currentPlayer = 0;
        CmpResult result = new CmpResult();

        while (true) {
            AiSolver.mmResult best = new AiSolver.mmResult();

            long start = bean.getCurrentThreadCpuTime();
            AiSolver.minimax(boards[currentPlayer], Integer.MIN_VALUE,
                    Integer.MAX_VALUE, levels[currentPlayer], best);
            long end = bean.getCurrentThreadCpuTime();
            double time = (end - start) / 1e9; // convert to seconds

            totalTimes[currentPlayer] += time - timepermove; // compute excess time

            if (totalTimes[currentPlayer] > 0) { // usinf to much time
                levels[currentPlayer] = Math.max(1, levels[currentPlayer] - 1);
            } else if (totalTimes[currentPlayer] < 0) { // using too little time
                levels[currentPlayer] = Math.min(maxLevel, levels[currentPlayer] + 1);
            }

            if (best.currentMove == null) { // game over
                // return the winner based on the value of the board or break a tie based on
                // average time per move
                if (best.Values == 0) { // draw, break tie based on average time per move
                    result.winner = totalTimes[0] < totalTimes[1] ? boards[0] : boards[1];
                    result.loser = totalTimes[0] < totalTimes[1] ? boards[1] : boards[0];
                } else {
                    result.winner = best.Values > 0 ? boards[0] : boards[1];
                    result.loser = best.Values > 0 ? boards[1] : boards[0];
                }
                break;
            } else {
                try {
                    boards[currentPlayer].applyMove(best.currentMove);
                    boards[1 - currentPlayer].applyMove(best.currentMove);

                } catch (Board.InvalidMoveException e) {
                }
                currentPlayer = 1 - currentPlayer;
            }

        }
        return result;
    }
}