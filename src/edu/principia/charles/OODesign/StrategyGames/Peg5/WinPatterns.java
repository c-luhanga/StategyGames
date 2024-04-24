package edu.principia.charles.OODesign.StrategyGames.Peg5;

public class WinPatterns {
    // Constants to represent different pieces
    public static final byte PEG = 1;
    public static final byte OPEN_TUBE = 2;
    public static final byte CLOSED_TUBE = 3;

    // Winning patterns encoded as arrays of bytes
    public static final byte[][] WINNING_PATTERNS = {
            { PEG, PEG, OPEN_TUBE, PEG, PEG }, // PPtPP
            { PEG, OPEN_TUBE, PEG, OPEN_TUBE, PEG }, // PtPtP
            { PEG, OPEN_TUBE, OPEN_TUBE, OPEN_TUBE, PEG } // PtttP
    };

    /**
     * Check if the given line of pieces matches any winning pattern.
     * 
     * @param line An array of bytes representing a line of pieces on the board.
     * @return true if the line contains a winning pattern, false otherwise.
     */
    public static boolean isWinningLine(byte[] line) {
        // System.out.println("Checking line for win (WinPattern): " + line[0] + line[1]
        // + line[2] + line[3] + line[4]);
        if (line.length != 5)
            return false; // Only consider lines of exactly 5 pieces

        for (byte[] pattern : WINNING_PATTERNS) {
            if (matchesPattern(line, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares a line of pieces against a win pattern.
     * 
     * @param line    The current line of pieces from the board.
     * @param pattern A win pattern to compare against.
     * @return true if the line matches the pattern, false otherwise.
     */
    private static boolean matchesPattern(byte[] line, byte[] pattern) {
        // System.out.println(
        // "Matching pattern(WinPattern): " + pattern[0] + pattern[1] + pattern[2] +
        // pattern[3] + pattern[4]);
        for (int i = 0; i < line.length; i++) {
            if (line[i] != pattern[i]) {
                return false; // No match at this position
            }
        }
        return true; // All positions match the pattern
    }

    /**
     * Evaluate a line of pieces to determine its score based on near-complete
     * patterns.
     * 
     * @param line   An array of bytes representing a line of pieces on the board.
     * @param player The current player's piece type.
     * @return Score for this line based on the number of pieces in sequence.
     */
    public static int evaluateLineScore(byte[] line, byte player) {
        int score = 0;
        int consecutiveCount = 0;

        for (int i = 0; i < line.length; i++) {
            if (line[i] == player) {
                consecutiveCount++;
            } else {
                if (consecutiveCount == 4) {
                    score += 10; // Four in a row scores 10 points
                } else if (consecutiveCount == 3) {
                    score += 1; // Three in a row scores 1 point
                }
                consecutiveCount = 0; // Reset count
            }
        }

        // Check at the end of the line
        if (consecutiveCount == 4) {
            score += 10;
        } else if (consecutiveCount == 3) {
            score += 1;
        }

        return score;
    }
}
