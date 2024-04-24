package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connections Class Development Guide for Beehive Game
 * 
 * Overview:
 * The Connections class is responsible for managing the direct adjacency
 * relationships between cells on the Beehive game board. It should efficiently
 * determine if two cells are adjacent (share an edge) on the hexagonal grid.
 * 
 * Requirements:
 * - Efficiently store and query adjacency relationships between cells
 * identified by their Location.
 * - Support the game logic by providing methods to check if two cells are
 * adjacent.
 * 
 * Member Variables:
 * - adjacencyMap: A data structure (e.g., HashMap<Location, List<Location>>)
 * that maps each cell location to a list of locations that are adjacent to it.
 * 
 * Methods:
 * - addConnection(Location from, Location to): Adds a bidirectional adjacency
 * relationship between two cell locations.
 * - isAdjacent(Location from, Location to): Returns true if the 'to' Location
 * is adjacent to the 'from' Location.
 * - getAdjacentLocations(Location from): Returns a list of Locations that are
 * adjacent to the 'from' Location.
 * 
 * Additional Considerations:
 * - Consider the uniqueness of the hexagonal grid when implementing adjacency.
 * Unlike square grids, hexagonal grids have six neighbors for each cell.
 * - Ensure the adjacencyMap is symmetric; if A is adjacent to B, then B must
 * also be adjacent to A.
 */

public class Connections {
    Cell cell1;
    Cell cell2;

    public Connections(Cell c1, Cell c2) {
        this.cell1 = c1;
        this.cell2 = c2;
    }

}
