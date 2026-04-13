/**
 * Représente un coup dans le jeu de Breakthrough.
 *
 * Contrairement au TicTacToe géant où un coup = une case de destination,
 * ici un coup = case de départ + case d'arrivée (ex: "D6-D5").
 *
 * Convention interne des indices :
 *   row 0 = rangée visuelle 8 (zone de départ des NOIRS)
 *   row 7 = rangée visuelle 1 (zone de départ des ROUGES)
 *
 * Conversion : internalRow = 8 - visualRow
 */
public class Move {

    private int fromRow, fromCol;
    private int toRow,   toCol;
    private Piece captured; // Pièce capturée (pour le backtracking unplay)

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow  = fromRow;
        this.fromCol  = fromCol;
        this.toRow    = toRow;
        this.toCol    = toCol;
        this.captured = Piece.VIDE;
    }

    // ── Getters ──────────────────────────────────────────────────
    public int   getFromRow()  { return fromRow;  }
    public int   getFromCol()  { return fromCol;  }
    public int   getToRow()    { return toRow;    }
    public int   getToCol()    { return toCol;    }
    public Piece getCaptured() { return captured; }
    public void  setCaptured(Piece p) { this.captured = p; }

    /**
     * Convertit le coup en format serveur : ex. "D6-D5"
     * (indices internes → rangées visuelles)
     */
    @Override
    public String toString() {
        char fromColChar = (char) ('A' + fromCol);
        int  fromRowVisual = 8 - fromRow;
        char toColChar   = (char) ('A' + toCol);
        int  toRowVisual   = 8 - toRow;
        return "" + fromColChar + fromRowVisual + "-" + toColChar + toRowVisual;
    }
}
