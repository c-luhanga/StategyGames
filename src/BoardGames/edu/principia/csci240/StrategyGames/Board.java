package boardgames.edu.principia.csci240.strategygames;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

// Represents a game board. The board is responsible for maintaining the state 
// of the game and enforcing the rules of the game.  
public interface Board {
   public static final int PLAYER_1 = 1;
   public static final int PLAYER_2 = -1;
   public static final int WIN = 1000000;

   // Represents one Move on the board.
   public interface Move extends Comparable<Move> {
      // Write the move to an OutputStream in binary format
      public void write(OutputStream os) throws java.io.IOException;

      // Read the move from an InputStream in binary format
      public void read(InputStream is) throws java.io.IOException;

      // Read the move from a String
      public void fromString(String s) throws java.io.IOException;
   }

   // Exception thrown when an invalid move is attempted
   public class InvalidMoveException extends Exception {
      public InvalidMoveException(String message) {
         super(message);
      }
   }

   // Factory method to create a new move
   public Move createMove();

   // Apply a move to the board throwing an exception if the move is invalid.
   public void applyMove(Move m) throws InvalidMoveException;

   // Return an integer indicating whether player 0 (negative value)
   // or player 1 (positive value) is winning, or 0 if the game is a draw
   // (0 value). A value of WIN indicates a win for player 0, and -WIN
   // indicates a win for player 1.
   public int getValue();

   // Return a list of all valid moves for the current player. An empty list
   // indicates that the game is over.
   public List<? extends Move> getValidMoves();

   // Return 1 if player 0 is to move, -1 if player 1 is to move
   public int getCurrentPlayer();

   // Return a history of all moves thus far applied to the board
   public List<? extends Move> getMoveHistory();

   // Undo most recent move, or do nothing if no moves have been made
   public void undoMove();

}
