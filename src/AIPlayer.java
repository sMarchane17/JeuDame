import java.util.List;

/**
 * Intelligence artificielle pour le jeu de Breakthrough.
 *
 * Implémente :
 *  - Algorithme Alpha-Beta (extension de Minimax avec élagage)
 *  - Approfondissement itératif (Iterative Deepening) pour utiliser
 *    au mieux la limite de temps de 5 secondes
 *  - Fonction d'évaluation heuristique multi-critères
 *
 * Architecture similaire à AIPlayer.java du TicTacToe géant.
 */
public class AIPlayer {

    private final Piece myPiece;       // couleur de l'IA (RED ou BLACK)
    private final long  timeLimitMs;   // limite de temps en millisecondes (5000)
    private static final int MAX_DEPTH = 8; // profondeur maximale absolue

    // ── Pondérations de l'évaluation ────────────────────────────
    private static final int MATERIAL_SCORE    = 100; // valeur d'une pièce
    private static final int ADVANCE_FACTOR    = 15;  // bonus par case avancée
    private static final int WIN_THREAT_BONUS  = 500; // pièce à 1 case de gagner
    private static final int PROTECT_BONUS     = 10;  // pièce protégée par une alliée

    public AIPlayer(Piece myPiece, long timeLimitMs) {
        this.myPiece     = myPiece;
        this.timeLimitMs = timeLimitMs;
    }

    // ── Interface publique ───────────────────────────────────────

    /**
     * Retourne le meilleur coup à jouer en utilisant
     * l'approfondissement itératif avec Alpha-Beta.
     *
     * On commence à profondeur 1 et on augmente jusqu'à ce que
     * le temps soit écoulé.  On garde toujours le meilleur coup
     * de la dernière profondeur COMPLÈTE.
     */
    public Move getBestMove(Board board) {
        long startTime = System.currentTimeMillis();

        List<Move> moves = board.generateMoves(myPiece);
        if (moves.isEmpty()) return null;

        Move bestMove = moves.get(0); // coup de secours

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {

            // Vérifie qu'il reste assez de temps pour explorer cette profondeur
            if (System.currentTimeMillis() - startTime >= timeLimitMs - 300) break;

            Move  currentBest  = moves.get(0);
            int   bestValue    = Integer.MIN_VALUE;
            int   alpha        = Integer.MIN_VALUE;
            int   beta         = Integer.MAX_VALUE;

            for (Move move : moves) {
                board.play(move);
                int value = alphaBeta(board, 1, depth, startTime,
                                      alpha, beta,
                                      false, getOpponent(myPiece));
                board.unplay(move);

                if (value > bestValue) {
                    bestValue   = value;
                    currentBest = move;
                }
                alpha = Math.max(alpha, bestValue);
                if (alpha >= beta) break; // coupure beta au niveau racine
            }

            // On ne met à jour bestMove que si on a fini la profondeur
            if (System.currentTimeMillis() - startTime < timeLimitMs - 300) {
                bestMove = currentBest;
                System.out.println("[IA] Profondeur " + depth
                        + " terminée | meilleur coup = " + bestMove
                        + " (score=" + bestValue + ")"
                        + " | temps=" + (System.currentTimeMillis()-startTime) + "ms");
            }
        }
        return bestMove;
    }

    // ── Alpha-Beta récursif ──────────────────────────────────────

    private int alphaBeta(Board board,
                          int depth,
                          int maxDepth,
                          long startTime,
                          int alpha,
                          int beta,
                          boolean isMaximizing,
                          Piece currentPiece) {

        // Garde-fou temporel
        if (System.currentTimeMillis() - startTime >= timeLimitMs - 300) {
            return evaluate(board);
        }

        // État terminal : quelqu'un a gagné
        Piece winner = board.checkWinner();
        if (winner == myPiece)            return  10000 - depth;
        if (winner == getOpponent(myPiece)) return -10000 + depth;

        // Profondeur maximale atteinte → évaluation statique
        if (depth >= maxDepth) {
            return evaluate(board);
        }

        List<Move> moves = board.generateMoves(currentPiece);
        if (moves.isEmpty()) return evaluate(board);

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (Move move : moves) {
                board.play(move);
                int val = alphaBeta(board, depth + 1, maxDepth, startTime,
                                    alpha, beta, false, getOpponent(currentPiece));
                board.unplay(move);
                best  = Math.max(best, val);
                alpha = Math.max(alpha, best);
                if (alpha >= beta) break; // coupure β
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Move move : moves) {
                board.play(move);
                int val = alphaBeta(board, depth + 1, maxDepth, startTime,
                                    alpha, beta, true, getOpponent(currentPiece));
                board.unplay(move);
                best = Math.min(best, val);
                beta = Math.min(beta, best);
                if (beta <= alpha) break; // coupure α
            }
            return best;
        }
    }

    // ── Fonction d'évaluation ────────────────────────────────────

    /**
     * Évalue la position du point de vue de myPiece.
     * Score positif = favorable à l'IA, négatif = défavorable.
     *
     * Critères (du plus important au moins important) :
     *  1. Matériel    : nombre de pièces (perdre une pièce = très mauvais)
     *  2. Avancement  : pièces proches du but adversaire (avance = progression)
     *  3. Menace      : pièce à 1 case de gagner (urgence critique)
     *  4. Protection  : pièce couverte par une alliée (sécurité)
     */
    private int evaluate(Board board) {
        int score = 0;
        Piece opponent = getOpponent(myPiece);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getCell(r, c);
                if (p == Piece.VIDE) continue;

                boolean isMine = (p == myPiece);
                int sign = isMine ? +1 : -1;

                // 1. Matériel
                score += sign * MATERIAL_SCORE;

                // 2. Avancement (0 = départ, 7 = un pas avant le but)
                int advancement = advancementOf(p, r);
                score += sign * advancement * ADVANCE_FACTOR;

                // 3. Menace de victoire (avancement == 7 → à 1 case du bord)
                if (advancement == 7) {
                    score += sign * WIN_THREAT_BONUS;
                }

                // 4. Protection : pièce couverte diagonalement par une alliée derrière elle
                if (isProtected(board, r, c, p)) {
                    score += sign * PROTECT_BONUS;
                }
            }
        }
        return score;
    }

    /**
     * Niveau d'avancement d'une pièce (0 = rangée de départ, 7 = rangée devant le but).
     * RED  : avance en diminuant row → advancement = 7 - row (départ row=7, but row=0)
     * BLACK: avance en augmentant row → advancement = row   (départ row=0, but row=7)
     */
    private int advancementOf(Piece p, int row) {
        return (p == Piece.ROUGE) ? (7 - row) : row;
    }

    /**
     * Vérifie si une pièce est protégée par une alliée positionnée
     * diagonalement derrière elle (dans la direction de recul).
     */
    private boolean isProtected(Board board, int row, int col, Piece piece) {
        // La rangée "derrière" est celle d'où la pièce vient
        int behindRow = (piece == Piece.ROUGE) ? row + 1 : row - 1;
        if (behindRow < 0 || behindRow >= 8) return false;

        if (col - 1 >= 0 && board.getCell(behindRow, col - 1) == piece) return true;
        if (col + 1 <  8 && board.getCell(behindRow, col + 1) == piece) return true;
        return false;
    }

    // ── Utilitaire ───────────────────────────────────────────────

    private Piece getOpponent(Piece p) {
        return (p == Piece.ROUGE) ? Piece.NOIR : Piece.ROUGE;
    }
}
