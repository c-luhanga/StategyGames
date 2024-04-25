package edu.principia.charles.OODesign.StrategyGames.Peg5;

public class WinPatterns {
    private static final byte NONE = 0;
    private static final byte GREEN_PEG = 1;
    private static final byte YELLOW_PEG = 2;
    private static final byte OPEN_GREEN_TUBE = 3;
    private static final byte CLOSED_GREEN_TUBE = 4;
    private static final byte OPEN_YELLOW_TUBE = 5;
    private static final byte CLOSED_YELLOW_TUBE = 6;

    private static final int WIN = 1000;

    public static boolean isWinningLine(byte[] line, byte player) {
        if (line.length != 5)
            return false;
        for (byte[] pattern : getPatternsForPlayer(player)) {
            if (matchesPattern(line, pattern, player)) {
                return true;
            }
        }
        return false;
    }

    private static byte[][] getPatternsForPlayer(byte player) {
        byte peg, openTube;
        if (player == GREEN_PEG || player == OPEN_GREEN_TUBE) {
            peg = GREEN_PEG;
            openTube = OPEN_GREEN_TUBE;
        } else {
            peg = YELLOW_PEG;
            openTube = OPEN_YELLOW_TUBE;
        }
        return new byte[][] {
                { peg, peg, openTube, peg, peg },
                { peg, openTube, peg, openTube, peg },
                { peg, openTube, openTube, openTube, peg }
        };
    }

    private static boolean matchesPattern(byte[] line, byte[] pattern, byte player) {
        for (int i = 0; i < line.length; i++) {
            if (!matchesPieceType(line[i], pattern[i], player)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesPieceType(byte piece, byte type, byte player) {
        // Closed tubes block the opponent's pieces but do not affect the player's own
        // pieces
        byte opponentClosedTube = (player == GREEN_PEG) ? CLOSED_YELLOW_TUBE : CLOSED_GREEN_TUBE;
        if (piece == type) {
            return true;
        }
        if (type == OPEN_GREEN_TUBE || type == OPEN_YELLOW_TUBE) {
            return (piece == OPEN_GREEN_TUBE || piece == OPEN_YELLOW_TUBE);
        }
        if (piece == opponentClosedTube) {
            return false; // If it's a closed tube for the opponent, it blocks the pattern
        }
        return false;
    }

    public static int evaluateLineScore(byte[] line, byte player) {
        int score = 0;
        for (int start = 0; start <= line.length - 5; start++) {
            int end = start + 5;
            byte[] segment = new byte[5];
            System.arraycopy(line, start, segment, 0, 5);
            if (isWinningLine(segment, player)) {
                score += WIN;
            } else {
                score += evaluatePartialScore(segment, player);
            }
        }
        return score;
    }

    private static int evaluatePartialScore(byte[] segment, byte player) {
        int count3 = 0, count4 = 0;
        for (int start = 0; start < segment.length; start++) {
            int end = start + 4;
            if (end < segment.length) {
                byte[] subsegment = new byte[4];
                System.arraycopy(segment, start, subsegment, 0, 4);
                int matchCount = countMatching(subsegment, player);
                if (matchCount == 3 && isPlayablePosition(segment, start + 4, player)) {
                    count3++;
                } else if (matchCount == 4 && isPlayablePosition(segment, start + 4, player)) {
                    count4++;
                }
            }
        }
        return count4 * 10 + count3;
    }

    private static int countMatching(byte[] segment, byte player) {
        int count = 0;
        for (byte b : segment) {
            if (b == player)
                count++;
        }
        return count;
    }

    private static boolean isPlayablePosition(byte[] segment, int index, byte player) {
        return index < segment.length && segment[index] == NONE;
    }
}
