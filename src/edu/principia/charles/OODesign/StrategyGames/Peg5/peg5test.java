// package edu.principia.charles.OODesign.StrategyGames.Peg5;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import edu.principia.OODesign.StrategyGames.Board;
// import edu.principia.charles.OODesign.StrategyGames.Peg5.Peg5Board.Peg5Move;

// import static org.junit.jupiter.api.Assertions.*;

// class Peg5BoardTest {
// private Peg5Board board;

// @BeforeEach
// void setUp() {
// board = new Peg5Board(); // Assuming Peg5Board is properly imported or
// defined in the same package
// }

// @Test
// void testInitialBoardSetup() {
// // Test the initial state of the board
// for (int row = 0; row < Peg5Board.BOARD_SIZE; row++) {
// for (int col = 0; col < Peg5Board.BOARD_SIZE; col++) {
// assertEquals(0, board.board[row][col], "Board should be initialized to
// empty");
// }
// }
// assertEquals(Board.PLAYER_0, board.getCurrentPlayer(), "Initial player should
// be PLAYER_0");
// }

// @Test
// void testCreateMove() {
// assertNotNull(board.createMove(), "createMove should not return null");
// }

// @Test
// void testApplyMoveAndUndo() throws Board.InvalidMoveException {
// // Test applying and undoing moves on the board
// Peg5Move move = (Peg5Move) board.createMove();
// // Configure move as needed based on your game's logic
// move.type = 1; // Assuming type '1' is a valid type for Player 0
// move.position = new Position(0, 0, (byte) 0); // Position the move at the
// start of the board
// board.applyMove(move);
// assertEquals(1, board.getMoveHistory().size(), "Move history size should be 1
// after applying a move");

// // Undo the move
// board.undoMove();
// assertTrue(board.getMoveHistory().isEmpty(), "Move history should be empty
// after undoing the move");
// }

// @Test
// void testInvalidMoveApplication() {
// Peg5Move move = (Peg5Move) board.createMove();
// move.position = new Position(-1, -1, (byte) 0); // Invalid position
// assertThrows(Board.InvalidMoveException.class, () -> board.applyMove(move),
// "Applying an invalid move should throw an InvalidMoveException");
// }

// @Test
// void testValidMovesList() {
// assertFalse(board.getValidMoves().isEmpty(), "Valid moves list should not be
// empty at the start of the game");
// }

// @Test
// void testGetValue() {
// assertEquals(0, board.getValue(), "Initial board value should be 0");
// }

// @Test
// void testGetCurrentPlayer() {
// assertEquals(Board.PLAYER_0, board.getCurrentPlayer(), "Initial player should
// be PLAYER_0");
// }

// @Test
// void testGetMoveHistory() {
// assertTrue(board.getMoveHistory().isEmpty(), "Move history should be empty at
// the start of the game");
// }

// @Test
// void testToString() {
// assertNotNull(board.toString(), "toString should not return null");
// assertTrue(board.toString().contains("Current Player: Green"), "toString
// should indicate who's turn it is");
// }

// @Test
// void testPeg5MoveToString() {
// Peg5Move move = (Peg5Move) board.createMove(); // Cast to Peg5Move
// move.position = new Position(3, 4, (byte) 0);
// assertEquals("Peg5Move at (4, 5)", move.toString(), "Peg5Move toString should
// return 'Peg5Move at (4, 5)'");
// }

// // @Test
// // void testPieceEncoding() {
// // byte cellState = encodePiece(1, 3); // Green peg in an open yellow tube
// // assertEquals("Gy", pieceToString(cellState), "Should encode and decode to
// // 'Gy'");
// // }

// }
