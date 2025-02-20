package BoardGames.edu.principia.csci240.StrategyGames.cluhanga.TicTacToe;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TTTBoardTest {
    @Test
    public void testMove() {
        assertEquals(2, 1 + 1); // basic first test that JUnit is working
        TTTBoard board = new TTTBoard();
        TTTBoard.TicTacToeMove move = board.new TicTacToeMove(1, 2);
        assertEquals(1, move.row);
        assertEquals(2, move.col);
    }
}