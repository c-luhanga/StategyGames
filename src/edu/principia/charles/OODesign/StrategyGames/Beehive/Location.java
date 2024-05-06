package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    public int row;
    public int col;

    public Location(int r, int c) {
        row = r;
        col = c;
    }

    public Location(Location l) {
        row = l.row;
        col = l.col;
    }

    public Location add(Location l) {
        return new Location(row + l.row, col + l.col);
    }

    public boolean inBounds() {
        return row >= 0 && row < 11 && col >= 0 && col < 11;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Location location = (Location) obj;
        return row == location.row && col == location.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
