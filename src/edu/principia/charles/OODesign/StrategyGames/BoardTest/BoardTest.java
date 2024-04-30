package edu.principia.charles.OODesign.StrategyGames.BoardTest;

import java.util.Scanner;

import edu.principia.OODesign.StrategyGames.Board.Board;
import edu.principia.charles.OODesign.StrategyGames.AiSolver.AiSolver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoardTest {
    private Board currentBoard;
    private Board.Move currentMove;
    private Class<?> boardClass;
    private List<Board.Move> moveHistory = new ArrayList<>();

    public static void main(String[] args) throws Board.InvalidMoveException {
        if (args.length != 1) {
            System.out.println("Usage: java BoardTest <BoardClassName>");
            return;
        }

        BoardTest boardTest = new BoardTest();
        boardTest.initializeBoardAndMove(args[0]);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            switch (command) {
                case "showBoard":
                    boardTest.showBoard();
                    break;
                case "showMoves":
                    boardTest.showMoves();
                    break;
                case "showMove":
                    boardTest.showMove();
                    break;
                case "applyMove":
                    boardTest.applyMove();
                    break;
                case "showVal":
                    boardTest.showVal();
                    break;
                case "showMoveHist":
                    boardTest.showMoveHist();
                    break;

                case "quit":
                    return;
                default:
                    if (command.startsWith("enterMove")) {
                        boardTest.enterMove(command.substring(10));
                    } else if (command.startsWith("doMove")) {
                        boardTest.doMove(command.substring(7));
                    } else if (command.startsWith("undoMoves")) {
                        boardTest.undoMoves(Integer.parseInt(command.substring(10)));
                    } else if (command.startsWith("saveBoard")) {
                        boardTest.saveBoard(command.substring(10));
                    } else if (command.startsWith("loadBoard")) {
                        boardTest.loadBoard(command.substring(10));
                    } else if (command.startsWith("compareMove")) {
                        boardTest.compareMove(command.substring(12));
                    } else if (command.startsWith("testPlay")) {
                        String[] parts = command.split(" ");
                        boardTest.testPlay(Long.parseLong(parts[1]), Integer.parseInt(parts[2]));
                    } else if (command.startsWith("testRun")) {
                        String[] parts = command.split(" ");
                        boardTest.testRun(Long.parseLong(parts[1]), Integer.parseInt(parts[2]));
                    } else if (command.startsWith("AiTest")) {
                        // Get the level of the AI
                        String levelString = command.substring(7).trim();
                        int level = Integer.parseInt(levelString);

                        AiSolver.mmResult result = new AiSolver.mmResult();
                        AiSolver.minimax(boardTest.currentBoard, Integer.MIN_VALUE, Integer.MAX_VALUE,
                                level, result);
                        if (result.currentMove != null) {
                            boardTest.doMove(result.currentMove.toString());
                        } else {
                            System.out.println("Unknown command: " + command);
                        }
                    } else if (command.startsWith("aiPlay")) {
                        // automatically play the game against itself
                        String[] parts = command.split(" ");
                        int level = Integer.parseInt(parts[1]);
                        int moveCount = Integer.parseInt(parts[2]);
                        boardTest.testAiPlay(level, moveCount);
                    } else {
                        System.out.println("Unknown command: " + command);
                    }
            }
        }
    }

    private void initializeBoardAndMove(String className) {
        // Implement initialization logic using reflection
        // Example:
        // Class<?> boardClass = Class.forName(className);
        // currentBoard = (Board) boardClass.getDeclaredConstructor().newInstance();
        // currentMove = currentBoard.createMove();
        try {
            boardClass = Class.forName(className);
            Constructor<?>[] constructors = boardClass.getConstructors();
            if (constructors.length == 0) {
                throw new Exception("No public constructors found for class " + className);
            }
            // Use the first public constructor found
            Constructor<?> constructor = constructors[0];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                // Provide default values for common types
                if (paramTypes[i] == int.class) {
                    params[i] = 0;
                } else if (paramTypes[i] == String.class) {
                    params[i] = "";
                } else {
                    // For other types, try to create a new instance
                    params[i] = paramTypes[i].getConstructor().newInstance();
                }
            }
            currentBoard = (Board) constructor.newInstance(params);
            currentMove = currentBoard.createMove();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Implement the commands as methods here
    // For example:

    private void showBoard() {
        System.out.println(currentBoard.toString());
    }

    private void showMoves() {
        /*
         * Prints a list of strings describing all moves possible from the current
         * board. Calculates the largest move size in the list of possible moves,
         * and then figures how many columns of moves of that largest size can be
         * fit onto an 80-char screen, with one space after each column. It then
         * prints all the moves using that many columns, with moves left-justified
         * within their column, so that they line up neatly.
         */
        List<? extends Board.Move> moves = currentBoard.getValidMoves();
        int maxMoveSize = 0;
        for (Board.Move move : moves) {
            String moveStr = move.toString();
            maxMoveSize = Math.max(maxMoveSize, moveStr.length());
        }
        int numCols = 80 / (maxMoveSize + 1);
        int col = 0;
        for (Board.Move move : moves) {
            System.out.print(move.toString());
            for (int i = move.toString().length(); i < maxMoveSize; i++) {
                System.out.print(" ");
            }
            col++;
            if (col == numCols) {
                System.out.println();
                col = 0;
            } else {
                System.out.print(" ");
            }
        }
        if (col != 0) {
            System.out.println();
        }
    }

    private void enterMove(String moveString) {
        // Assign the contents of moveString, which will fall on the same line, into the
        // current move.
        try {
            currentMove.fromString(moveString);
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showMove() {
        // Print the string-conversion of the current move
        System.out.println(currentMove.toString());
    }

    private void applyMove() {
        // Apply the current move to the current board. If the current move is not one
        // of the allowed moves for the board, print "Not a permitted move", followed by
        // a list of all the allowed moves, as in showBoard.
        // Note, applyMove does not clear the current move. A showMove call after
        // applyMove still shows the move.
        try {
            currentBoard.applyMove(currentMove);
        } catch (Board.InvalidMoveException e) {
            System.out.println("Not a permitted move");
            showMoves();
        }
    }

    private void doMove(String move) {
        System.out.println("Doing move: " + move);
        enterMove(move);
        applyMove();
        moveHistory.add(currentMove);
        currentMove = currentBoard.createMove();
    }

    private void undoMoves(int numMoves) {
        int movesToUndo = Math.min(numMoves, moveHistory.size());
        for (int i = 0; i < movesToUndo; i++) {
            System.out.println("Undoing move: " + moveHistory.get(moveHistory.size() - 1));
            currentBoard.undoMove();
            moveHistory.remove(moveHistory.size() - 1);
        }
        currentMove = moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }

    private void showVal() {
        // Show the value of the current board.
        System.out.println(currentBoard.getValue());
        int value = currentBoard.getValue();
        System.out.println("Current board value: " + value);
        if (value == 1000000) {
            System.out.println("Player 0 wins");
        } else if (value == -1000000) {
            System.out.println("Player 1 wins");
        }
    }

    private void showMoveHist() {
        // Show a list of the moves made thus far on the current board, using the same
        // column-sizing logic as described in showMoves
        List<? extends Board.Move> moves = currentBoard.getMoveHistory();
        int maxMoveSize = 0;
        for (Board.Move move : moves) {
            maxMoveSize = Math.max(maxMoveSize, move.toString().length());
        }
        int numCols = 80 / (maxMoveSize + 1);
        int col = 0;
        for (Board.Move move : moves) {
            System.out.print(move.toString());
            for (int i = move.toString().length(); i < maxMoveSize; i++) {
                System.out.print(" ");
            }
            col++;
            if (col == numCols) {
                System.out.println();
                col = 0;
            } else {
                System.out.print(" ");
            }
        }
        if (col != 0) {
            System.out.println();
        }
    }

    private void saveBoard(String filename) {
        // Open fileName and write the binary contents of the current board or move into
        // it. For the current board, simply write all the moves in its history. This
        // list will suffice to rebuild the board when reading in
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(currentBoard.getMoveHistory()); // Save moveHistory instead of getMoveHistory
            objectOut.close();
            System.out.println("Board saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving board: " + e.getMessage());
        }
    }

    private void loadBoard(String filename) throws Board.InvalidMoveException {
        // Open fileName and read binary contents from it into the current board or
        // move. In the case of loading a board, start with a new default-constructed
        // current board, and apply all the moves in the file to it.
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            List<? extends Board.Move> moves = (List<? extends Board.Move>) objectIn.readObject();
            objectIn.close();

            // Start with a new default-constructed current board
            currentBoard = (Board) boardClass.getDeclaredConstructor().newInstance();
            moveHistory.clear(); // Clear moveHistory before loading moves

            // Apply all the moves in the file to the current board
            for (Board.Move move : moves) {
                // doMove(move.toString()); // Use doMove to apply moves and update moveHistory
                // currentMove = move;
                currentBoard.applyMove(move);
            }

            System.out.println("Board loaded successfully.");
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            System.out.println("Error loading board: " + e.getMessage());
        }
    }

    private void compareMove(String moveString) {
        // Compare the current move with the move described by moveString. Print
        // "Current move is less", "Current move is equal", or "Current move is greater"
        Board.Move otherMove = currentBoard.createMove();
        try {
            otherMove.fromString(moveString);
            if (currentMove == null) {
                System.out.println("Current move is not set");
            } else {
                int result = currentMove.compareTo(otherMove);
                if (result < 0) {
                    System.out.println("Current move is less");
                } else if (result > 0) {
                    System.out.println("Current move is greater");
                } else {
                    System.out.println("Current move is equal");
                }
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void testPlay(long seed, int moveCount) {
        // Generate moveCount randomly-selected moves via the following procedure.
        // Create a java.util.Random object rnd, constructed with seed. Then repeatedly
        // (moveCount times) call GetAllMoves and select the Nth move, where N is the
        // rnd.nextInt value, limited to the size of the allowed move list. Apply this
        // move to the board. End the loop early and without complaint if the game
        // reaches its end.
        // Random rnd = new Random(seed);
        Random rnd = new Random(seed);
        for (int i = 0; i < moveCount; i++) {
            List<? extends Board.Move> moves = currentBoard.getValidMoves();
            if (moves.isEmpty()) {
                System.out.println("Game is a draw.");
                showBoard();
                break;
            }
            int n = rnd.nextInt(moves.size());
            try {
                currentBoard.applyMove(moves.get(n));
                moveHistory.add(moves.get(n));
                if (Math.abs(currentBoard.getValue()) == 1000000) {
                    System.out.println(
                            "Game has ended. Player " + (currentBoard.getValue() > 0 ? "1" : "2") + " has won.");
                    showBoard();
                    break;
                }
            } catch (Board.InvalidMoveException e) {
                System.out.println("Not a permitted move");
                showMoves();
            }
        }
    }

    private void testRun(long seed, int stepCount) {
        // Like testPlay, but each time the game ends, randomly select a number between
        // 1 and the current number of moves, retract that many moves, and proceed until
        // a total of stepCount "steps" have been made, where a step is either a single
        // forward move or a retraction of 1 or more moves.
        // This is intended as a speed check. You should be able to do a run with
        // 100,000 to 1,000,000 moves, depending on the game, in a reasonably short
        // time.
        Random rnd = new Random(seed);
        for (int i = 0; i < stepCount; i++) {
            List<? extends Board.Move> moves = currentBoard.getValidMoves();
            if (moves.isEmpty()) {
                int moveNum = rnd.nextInt(moveHistory.size()) + 1;
                for (int j = 0; j < moveNum; j++) {
                    currentBoard.undoMove();
                    moveHistory.remove(moveHistory.size() - 1);
                }
                currentMove = moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
            } else {
                int moveNum = rnd.nextInt(moves.size());
                try {
                    currentBoard.applyMove(moves.get(moveNum));
                    moveHistory.add(moves.get(moveNum));
                } catch (Board.InvalidMoveException e) {
                    System.out.println("Not a permitted move");
                    showMoves();
                }
            }
        }
    }

    private void testAi(int level) {
        AiSolver.mmResult result = new AiSolver.mmResult();
        AiSolver.minimax(currentBoard, Integer.MIN_VALUE, Integer.MAX_VALUE, level, result);
        if (result.currentMove != null) {
            doMove(result.currentMove.toString());
        }
    }

    private void testAiPlay(int level, int moveCount) {
        AiSolver.mmResult result = new AiSolver.mmResult();
        for (int i = 0; i < moveCount; i++) {
            AiSolver.minimax(currentBoard, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    level, result);
            if (result.currentMove == null) {
                break;
            }
            try {
                currentBoard.applyMove(result.currentMove);
            } catch (Board.InvalidMoveException e) {
                System.out.println("Invalid move");
            }
            {
                assert false;
            }
            System.out.println(
                    "Move: " + result.currentMove + " MM value 0" + " Board value " + currentBoard.getValue());
            System.out.println(currentBoard);
        }
    }

}