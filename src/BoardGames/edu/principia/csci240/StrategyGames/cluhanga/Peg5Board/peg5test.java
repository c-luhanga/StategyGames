package BoardGames.edu.principia.csci240.StrategyGames.cluhanga.Peg5Board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import BoardGames.edu.principia.csci240.StrategyGames.Board;

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
        assertEquals(Board.PLAYER_1, board.getCurrentPlayer(), "Initial player should be PLAYER_0");
    }

    @Test
    void testCreateMove() {
        Board.Move move = board.createMove();
        assertNotNull(move, "createMove should not return null");
    }

    @Test
    void testApplyMoveAndUndo() throws Board.InvalidMoveException {
        Board.Move move = board.createMove();
        // Configure move as needed based on your game's logic

        // Apply move
        board.applyMove(move);
        assertEquals(1, board.getMoveHistory().size(), "Move history size should be 1 after applying a move");

        // Undo move
        board.undoMove();
        assertTrue(board.getMoveHistory().isEmpty(), "Move history should be empty after undoing the move");
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

    // Additional tests can include specific game scenarios, testing winning
    // conditions, and more.
}
