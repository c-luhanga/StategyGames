package edu.principia.OODesign.StrategyGames.Tournament;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.Queue;

import edu.principia.OODesign.StrategyGames.Board;
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
    public static CmpResult runCompetition(Board brd0, Board brd1, int timepermove, int maxLevel) {
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

    static private class CmpNode {
        String name;
        Board brd;
        CmpNode left;
        CmpNode right;

        public CmpNode(String name, Board brd) {
            this.name = name;
            this.brd = brd;
        }

        // Run a tournament between the two subtrees of this node, and return the winner
        // setting the brd to the winner.
        // and name to the name of the winner.
        public CmpNode(CmpNode left, CmpNode right, double timepermove, int maxLevel) {
            this.left = left;
            this.right = right;
            CmpResult result = runCompetition(left.brd, right.brd, (int) timepermove, maxLevel);
            this.brd = result.winner;
            this.name = result.winner == left.brd ? left.name : right.name;
        }
    }
    // Run a tournament between players specified in a players file, with
    // time allowed per move timepermove, and max level maxLevel specified.. The
    // command line
    // arguments are the name of the players file, timepermove (as a double), and
    // maxLevel (as an int).
    // The file format is a sequence of lines, each containing a name and a class
    // name.
    // Classes must implement the Board interface. For each line, create a new
    // instance of the class.
    // generate a board from it, and construct a leaf CmpNode with the name and
    // board.

    // add each new CmpNode to a a queue. Then, repeatedly remove two CmpNodes from
    // the queue, and create a new CmpNode
    // with the winner of the competition between the two nodes.
    // Add the new CmpNode to the queue. Repeat until there is only one node in the
    // queue. Return the name of the winner.
    // and the root of the tree of CmpNodes.
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Tournament playersfile timepermove maxLevel");
            return;
        }
        String playersfile = args[0];
        double timepermove = Double.parseDouble(args[1]);
        int maxLevel = Integer.parseInt(args[2]);

        CmpNode root = null;
        Queue<CmpNode> queue = new java.util.LinkedList<CmpNode>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(playersfile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                String name = parts[0];
                String className = parts[1];
                System.out.println("Attempting to load class: " + className);
                try {
                    Class<?> cls = Class.forName(className);
                    Board brd = (Board) cls.getConstructor().newInstance();
                    queue.add(new CmpNode(name, brd));
                    System.out.println("Successfully loaded class: " + className);
                } catch (ClassNotFoundException e) {
                    System.out.println("Failed to load class: " + className);
                    e.printStackTrace();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException e) {
                    System.out.println("Failed to instantiate class: " + className);
                    e.printStackTrace();
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error reading players file: " + e);
            System.exit(1);
        }

        while (queue.size() > 1) {
            CmpNode left = queue.remove();
            CmpNode right = queue.remove();
            CmpNode winner = new CmpNode(left, right, timepermove, maxLevel);
            System.out.printf("%s vs %s: %s\n", left.name, right.name, winner.name);
            queue.add(winner);
        }

        System.out.println("Winner: " + queue.remove().name);
    }
}