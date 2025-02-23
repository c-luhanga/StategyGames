/*Board Driver Spec

1.0 Overview
Build a main function, and supporting functions, in a new class BoardDriver, that tests any implementation of Board and Board.Move. (Board.java is available under Canvas > Files.)

2.0 Main Program Start
The main program accepts one commandline argument giving the class name of the desired Board. For instance:

BoardDriver TTTBoard

Based on this, it sets up Board and Move pointers to objects of the relevant subclass (e.g. to a TTTBoard and TTTMove). These two objects are the "current board" and the "current move". It uses these to operate a main loop performing the commands specified below. It uses reflection or factory methods to create the current board and current move, and makes no mention of any of the specific games in the main source code.

3.0 The Commands

showBoard
Uses toString to get and print a textual picture of the current board.

showMoves
Prints a list of strings describing all moves possible from the current board. Calculates the largest move size in the list of possible moves, and then figures how many columns of moves of that largest size can be fit onto an 80-char screen, with one space after each column. It then prints all the moves using that many columns, with moves left-justified within their column, so that they line up neatly. 

enterMove moveString
Assign the contents of moveString, which will fall on the same line, into the current move.

showMove
Print the string-conversion of the current move.

applyMove
Apply the current move to the current board. If the current move is not one of the allowed moves for the board, print "Not a permitted move", followed by a list of all the allowed moves, as in showBoard.
Note, applyMove does not clear the current move. A showMove call after applyMove still shows the move.

doMove move
Combines the action of enterMove followed by applyMove, for convenience.

undoMoves count
Reverse the last count moves applied to the current board. Stop automatically if you reach the start of the game. This allows one to easily retract to game start by supplying a very large count.

showVal
Show the value of the current board.

showMoveHist
Show a list of the moves made thus far on the current board, using the same column-sizing logic as described in showMoves.
2 left-justified 40-character columns

saveBoard fileName
Open fileName and write the contents of the current board into it. For the current board, simply write all the moves in its history. This list will suffice to rebuild the board when reading in.

loadBoard fileName
Open fileName and read contents from it into the current board. In the case of loading a board, start with a new default-constructed current board, and apply all the moves in the file to it.

compareMove moveString
Compare the current move with the move described by moveString. Print "Current move is less", "Current move is equal", or "Current move is greater".

showPlayer
Display whose turn it is: 1 or -1

testPlay seed moveCount
Generate moveCount randomly-selected moves via the following procedure.
Create a java.util.Random object rnd, constructed with seed. Then repeatedly (moveCount times) call getValidMoves and select the Nth move, where N is the rnd.nextInt value, limited to the size of the allowed move list. Apply this move to the board. End the loop early and without complaint if the game reaches its end.

testRun seed stepCount
Like testPlay, but each time the game ends, randomly select a number between 1 and the current number of moves, retract that many moves, and proceed until a total of stepCount "steps" have been made, where a step is either a single forward move or a retraction of 1 or more moves.  Compute the number of retracted moves by randInt on the size of the moveHistory, plus 1.  (This results in a random value from 1 to moveHist.size() inclusive.)
This is intended as a speed check. You should be able to do a run with 100,000 to 1,000,000 moves, depending on the game, in a reasonably short time.

quit
End the main program.

 */
package boardgames.edu.principia.csci240.strategygames;

import java.io.*;
import java.util.*;

public class BoardDriver {
    private Board board;
    private Board.Move currentMove;
    private Scanner scanner;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: BoardDriver <BoardClassName>");
            return;
        }
        BoardDriver driver = new BoardDriver();
        driver.initializeBoardAndMove(args[0]);
        driver.runCommandLoop();
    }

    private void initializeBoardAndMove(String className) {
        try {
            Class<?> boardClass = Class.forName(className);
            board = (Board) boardClass.getDeclaredConstructor().newInstance();
            currentMove = board.createMove();
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void runCommandLoop() {
        while (true) {
            String line = scanner.nextLine();
            String[] tokens = line.split("\\s+");
            String command = tokens[0].toLowerCase();
            try {
                switch (command) {
                    case "showboard":
                        System.out.println(board.toString());
                        break;
                    case "showmoves":
                        showMoves(board.getValidMoves());
                        break;
                    case "entermove":
                        if (tokens.length > 1) {
                            currentMove.fromString(tokens[1]);
                        }
                        break;
                    case "showmove":
                        System.out.println(currentMove.toString());
                        break;
                    case "applymove":
                        try {
                            board.applyMove(currentMove);
                        } catch (Board.InvalidMoveException e) {
                            System.out.println("Not a permitted move");
                            showMoves(board.getValidMoves());
                        }
                        break;
                    case "domove":
                        if (tokens.length > 1) {
                            currentMove.fromString(tokens[1]);
                            try {
                                board.applyMove(currentMove);
                            } catch (Board.InvalidMoveException e) {
                                System.out.println("Not a permitted move");
                                showMoves(board.getValidMoves());
                            }
                        }
                        break;
                    case "undomoves":
                        if (tokens.length > 1) {
                            int count = Integer.parseInt(tokens[1]);
                            for (int i = 0; i < count && !board.getMoveHistory().isEmpty(); i++) {
                                board.undoMove();
                            }
                        }
                        break;
                    case "showval":
                        System.out.println("Current board value: " + board.getValue());
                        break;
                    case "showmovehist":
                        showMoveHistory();
                        break;
                    case "saveboard":
                        if (tokens.length > 1) {
                            saveBoard(tokens[1]);
                        }
                        break;
                    case "loadboard":
                        if (tokens.length > 1) {
                            loadBoard(tokens[1]);
                        }
                        break;
                    case "comparemove":
                        if (tokens.length > 1) {
                            compareMove(tokens[1]);
                        }
                        break;
                    case "showplayer":
                        System.out.println("Current player: " + board.getCurrentPlayer());
                        break;
                    case "testplay":
                        if (tokens.length > 2) {
                            testPlay(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2]));
                        }
                        break;
                    case "testrun":
                        if (tokens.length > 2) {
                            testRun(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2]));
                        }
                        break;
                    case "quit":
                        return;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            } catch (Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        }
    }

    private void showMoves(List<? extends Board.Move> moves) {
        if (moves.isEmpty()) {
            return;
        }

        // Find maximum move string length
        int maxLength = 0;
        for (Board.Move move : moves) {
            maxLength = Math.max(maxLength, move.toString().length());
        }

        // Calculate number of columns (80 chars width, space between columns)
        int numColumns = 80 / (maxLength + 1);
        int currentColumn = 0;

        for (Board.Move move : moves) {
            String moveStr = move.toString();
            System.out.print(String.format("%-" + maxLength + "s ", moveStr));
            currentColumn++;
            if (currentColumn >= numColumns) {
                System.out.println();
                currentColumn = 0;
            }
        }
        if (currentColumn > 0) {
            System.out.println();
        }
    }

    private void showMoveHistory() {
        List<? extends Board.Move> history = board.getMoveHistory();
        if (history.isEmpty()) {
            return;
        }

        // Use 2 columns of 40 characters each
        int currentColumn = 0;
        for (Board.Move move : history) {
            System.out.print(String.format("%-40s", move.toString()));
            currentColumn++;
            if (currentColumn >= 2) {
                System.out.println();
                currentColumn = 0;
            }
        }
        if (currentColumn > 0) {
            System.out.println();
        }
    }

    private void saveBoard(String fileName) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {
            List<? extends Board.Move> history = board.getMoveHistory();
            for (Board.Move move : history) {
                move.write(out);
            }
            System.out.println("Board saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving board: " + e.getMessage());
        }
    }

    private void loadBoard(String fileName) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(fileName))) {
            board = board.getClass().getDeclaredConstructor().newInstance();
            while (in.available() > 0) {
                Board.Move move = board.createMove();
                move.read(in);
                board.applyMove(move);
            }
            System.out.println("Board loaded successfully.");
        } catch (Exception e) {
            System.out.println("Error loading board: " + e.getMessage());
        }
    }

    private void compareMove(String moveString) throws IOException {
        Board.Move otherMove = board.createMove();
        otherMove.fromString(moveString);
        int comparison = currentMove.compareTo(otherMove);
        if (comparison < 0) {
            System.out.println("Current move is less");
        } else if (comparison > 0) {
            System.out.println("Current move is greater");
        } else {
            System.out.println("Current move is equal");
        }
    }

    private void testPlay(long seed, int moveCount) {
        Random rnd = new Random(seed);
        for (int i = 0; i < moveCount; i++) {
            List<? extends Board.Move> validMoves = board.getValidMoves();
            if (validMoves.isEmpty()) {
                break;
            }
            int moveIndex = rnd.nextInt(validMoves.size());
            try {
                board.applyMove(validMoves.get(moveIndex));
            } catch (Board.InvalidMoveException e) {
                // Should never happen as we're using valid moves
                break;
            }
        }
    }

    private void testRun(long seed, int stepCount) {
        Random rnd = new Random(seed);
        int steps = 0;
        while (steps < stepCount) {
            List<? extends Board.Move> validMoves = board.getValidMoves();
            if (validMoves.isEmpty()) {
                // Game ended, retract some moves
                List<? extends Board.Move> history = board.getMoveHistory();
                if (!history.isEmpty()) {
                    int movesToUndo = rnd.nextInt(history.size()) + 1;
                    for (int i = 0; i < movesToUndo && !history.isEmpty(); i++) {
                        board.undoMove();
                        steps++;
                    }
                }
            } else {
                // Make a move
                int moveIndex = rnd.nextInt(validMoves.size());
                try {
                    board.applyMove(validMoves.get(moveIndex));
                    steps++;
                } catch (Board.InvalidMoveException e) {
                    // Should never happen as we're using valid moves
                    break;
                }
            }
        }
    }
}