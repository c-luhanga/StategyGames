package edu.principia.charles.OODesign.StrategyGames.Beehive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class Connections implements Serializable {
    Cell cell1;
    Cell cell2;

    public Connections(Cell c1, Cell c2) {
        this.cell1 = c1;
        this.cell2 = c2;
    }
}
