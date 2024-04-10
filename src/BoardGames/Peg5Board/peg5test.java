package BoardGames.Peg5Board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import BoardGames.Board;

import static org.junit.jupiter.api.Assertions.*;

class peg5test {
    private Peg5Board board;

    @BeforeEach
    void setUp() {
        board = new Peg5Board();
    }

    @Test
    void testInitialBoardSetup() {
        // Assuming the initial setup of the board is all zeros (empty)
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                // This checks that the board is initialized correctly
                assertEquals(0, board.getBoard()[row][col], "Board should be initialized to empty");
            }
        }
        assertEquals(Board.PLAYER_0, board.getCurrentPlayer(), "Initial player should be PLAYER_0");
    }

    @Test
    void testCreateMove() {
        Board.Move move = board.createMove();
        assertNotNull(move, "createMove should not return null");
    }

    @Test
    // do a all valid moves and then undo them
    void testApplyMoveAndUndo() throws Board.InvalidMoveException {
        Board.Move move = board.createMove();
        // Configure move as needed based on your game's logic
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Peg5Board.Peg5Move peg5Move = (Peg5Board.Peg5Move) move;
                peg5Move.row = i;
                peg5Move.col = j;
                board.applyMove(move);
                move = board.createMove();
            }
        }
        assertTrue(board.getMoveHistory().size() == 49, "Move history size should be 49 after applying 49 moves");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                board.undoMove();
            }
        }
        assertTrue(board.getMoveHistory().isEmpty(), "Move history should be empty after undoing all moves");
        // // Apply move
        // board.applyMove(move);
        // assertEquals(1, board.getMoveHistory().size(), "Move history size should be 1
        // after applying a move");

        // // Undo move
        // board.undoMove();
        // assertTrue(board.getMoveHistory().isEmpty(), "Move history should be empty
        // after undoing the move");
    }

    @Test
    void testInvalidMoveType() {
        Board.Move move = board.createMove();
        // Set up the move to be of an invalid type
        assertThrows(Board.InvalidMoveException.class, () -> board.applyMove(move),
                "Applying an invalid move type should throw an InvalidMoveException");
    }

    @Test
    void testInvalidMoveApplication() {
        Board.Move move = board.createMove();
        // Set up the move to be invalid based on your game's logic
        assertThrows(Board.InvalidMoveException.class, () -> board.applyMove(move),
                "Applying an invalid move should throw an InvalidMoveException");
    }

    @Test
    void testValidMovesList() {
        // This test may need to be adjusted based on how your game logic determines
        // valid moves
        assertFalse(board.getValidMoves().isEmpty(), "Valid moves list should not be empty at the start of the game");
    }

    @Test
    void testSwitchPlayer() {
        board.switchPlayer(); // Assuming this method is accessible; if it's private, you might need to
                              // trigger it indirectly
        assertEquals(Board.PLAYER_1, board.getCurrentPlayer(),
                "Current player should switch to PLAYER_1 after switchPlayer is called");
    }

    @Test
    void testGetValue() {
        // This test will depend on your game logic for determining the value of the
        // board
        assertEquals(0, board.getValue(), "Initial board value should be 0");
    }

    @Test
    void testGetCurrentPlayer() {
        assertEquals(Board.PLAYER_0, board.getCurrentPlayer(), "Initial player should be PLAYER_0");
    }

    @Test
    void testGetMoveHistory() {
        assertTrue(board.getMoveHistory().isEmpty(), "Move history should be empty at the start of the game");
    }

    @Test
    void testToString() {
        // This test will depend on how you implement the toString method in your board
        // class
        assertNotNull(board.toString(), "toString should not return null");
    }

    @Test
    void testPeg5MoveToString() {
        Peg5Board.Peg5Move move = board.new Peg5Move(); // Fix: Instantiate Peg5Move using an instance of Peg5Board
        move.row = 3;
        move.col = 4;
        assertEquals("4,5", move.toString(), "Peg5Move toString should return '4,5'");
    }

}
