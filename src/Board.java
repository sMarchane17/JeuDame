import java.util.ArrayList;
import java.util.List;

/**
 * Plateau 8x8 du jeu de Breakthrough.
 *
 * Convention des indices internes :
 *   board[row][col]
 *   row 0 = rangée visuelle 8 (haut, départ des NOIRS)
 *   row 7 = rangée visuelle 1 (bas,  départ des ROUGES)
 *   col 0 = colonne A, col 7 = colonne H
 *
 * Direction de jeu :
 *   RED   avance vers row décroissant (vers row 0 = visuelle 8)
 *   BLACK avance vers row croissant   (vers row 7 = visuelle 1)
 *
 * Règles de déplacement :
 *   - Tout droit (vers l'avant) : seulement si la case est VIDE
 *   - Diagonal gauche/droite    : case vide OU capture d'un ennemi
 *   - Impossible de reculer
 *   - Impossible de capturer ses propres pièces
 */
public class Board {

    private Piece[][] cells;

    // ── Constructeur ─────────────────────────────────────────────

    public Board() {
        cells = new Piece[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                cells[r][c] = Piece.VIDE;
    }

    // ── Initialisation ───────────────────────────────────────────

    /** Configuration par défaut (identique à la Figure 1 de l'énoncé). */
    public void initDefault() {
        // Rangées visuelles 7-8 (internal 0-1) → NOIRS
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 8; c++)
                cells[r][c] = Piece.NOIR;
        // Rangées visuelles 1-2 (internal 6-7) → ROUGES
        for (int r = 6; r < 8; r++)
            for (int c = 0; c < 8; c++)
                cells[r][c] = Piece.ROUGE;
        // Milieu vide
        for (int r = 2; r < 6; r++)
            for (int c = 0; c < 8; c++)
                cells[r][c] = Piece.VIDE;
    }

    /**
     * Initialise le plateau depuis les 64 valeurs du serveur.
     * Le serveur envoie de haut en bas, gauche à droite.
     *   0 = vide, 2 = noir, 4 = rouge
     */
    public void setFromServerData(String[] values) {
        int idx = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int val = Integer.parseInt(values[idx++]);
                if      (val == 4) cells[r][c] = Piece.ROUGE;
                else if (val == 2) cells[r][c] = Piece.NOIR;
                else               cells[r][c] = Piece.VIDE;
            }
        }
    }

    // ── Accès aux cellules ───────────────────────────────────────

    public Piece getCell(int row, int col) {
        return cells[row][col];
    }

    public void setCell(int row, int col, Piece piece) {
        cells[row][col] = piece;
    }

    // ── Jouer / Annuler un coup (pour le backtracking Alpha-Beta) ─

    /**
     * Applique un coup sur le plateau.
     * Stocke la pièce éventuellement capturée dans l'objet Move
     * pour pouvoir annuler plus tard.
     */
    public void play(Move move) {
        Piece moving  = cells[move.getFromRow()][move.getFromCol()];
        Piece captured = cells[move.getToRow()][move.getToCol()];
        move.setCaptured(captured);                         // mémorise pour unplay
        cells[move.getToRow()][move.getToCol()]     = moving;
        cells[move.getFromRow()][move.getFromCol()] = Piece.VIDE;
    }

    /**
     * Annule le dernier coup joué (backtracking).
     * Restaure exactement l'état précédent grâce à move.getCaptured().
     */
    public void unplay(Move move) {
        Piece moving = cells[move.getToRow()][move.getToCol()];
        cells[move.getFromRow()][move.getFromCol()] = moving;
        cells[move.getToRow()][move.getToCol()]     = move.getCaptured();
    }

    // ── Condition de victoire ────────────────────────────────────

    /**
     * Vérifie s'il y a un gagnant.
     * RED gagne si une de ses pièces atteint row 0 (rangée visuelle 8).
     * BLACK gagne si une de ses pièces atteint row 7 (rangée visuelle 1).
     *
     * @return RED, BLACK, ou EMPTY (partie en cours)
     */
    public Piece checkWinner() {
        // row 0 = rangée visuelle 8 → victoire RED
        for (int c = 0; c < 8; c++)
            if (cells[0][c] == Piece.ROUGE) return Piece.ROUGE;

        // row 7 = rangée visuelle 1 → victoire BLACK
        for (int c = 0; c < 8; c++)
            if (cells[7][c] == Piece.NOIR) return Piece.NOIR;

        return Piece.VIDE;
    }

    // ── Génération des coups ─────────────────────────────────────

    /**
     * Génère tous les coups valides pour le joueur donné.
     *
     * RED  se déplace vers row décroissant (dir = -1)
     * BLACK se déplace vers row croissant  (dir = +1)
     *
     * Règles :
     *  - Tout droit (newRow, col)     : uniquement si la case est VIDE
     *  - Diagonal (newRow, col ± 1)   : si la case est VIDE ou ennemie (capture)
     *  - Ne peut pas aller sur sa propre pièce
     */
    public List<Move> generateMoves(Piece player) {
        List<Move> moves = new ArrayList<>();
        int dir = (player == Piece.ROUGE) ? -1 : 1;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (cells[r][c] != player) continue;

                int nr = r + dir; // nouvelle rangée
                if (nr < 0 || nr >= 8) continue; // bord du plateau

                // 1. Tout droit (uniquement si vide — pas de capture en avant)
                if (cells[nr][c] == Piece.VIDE) {
                    moves.add(new Move(r, c, nr, c));
                }

                // 2. Diagonale gauche (vide ou capture ennemie)
                if (c - 1 >= 0 && cells[nr][c - 1] != player) {
                    moves.add(new Move(r, c, nr, c - 1));
                }

                // 3. Diagonale droite (vide ou capture ennemie)
                if (c + 1 < 8 && cells[nr][c + 1] != player) {
                    moves.add(new Move(r, c, nr, c + 1));
                }
            }
        }
        return moves;
    }

    // ── Affichage console (debug) ────────────────────────────────

    public void printBoard() {
        System.out.println("  A B C D E F G H");
        for (int r = 0; r < 8; r++) {
            System.out.print((8 - r) + " ");
            for (int c = 0; c < 8; c++) {
                switch (cells[r][c]) {
                    case ROUGE:   System.out.print("R "); break;
                    case NOIR: System.out.print("N "); break;
                    default:    System.out.print(". "); break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
