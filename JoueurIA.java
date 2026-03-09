// JoueurIA.java
import java.util.ArrayList;

public class JoueurIA {

    // Méthode qui génère tous les coups possibles pour un joueur donné
    public static ArrayList<Coup> genererMouvements(int[][] board, int joueur) {
        // Pré-allocation pour la performance (un joueur a rarement plus de 40 coups)
        ArrayList<Coup> mouvementsPossibles = new ArrayList<>(40);
        
        // Détermination de la direction d'avancement.
        // Les rouges (4) sont en bas et montent (direction -1).
        // Les noirs (2) sont en haut et descendent (direction +1).
        int direction = (joueur == 4) ? -1 : 1;
        
        // Parcours complet du plateau (8x8)
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                
                // Si on trouve un pion appartenant au joueur qui doit jouer
                if (board[l][c] == joueur) {
                    int nl = l + direction; // nl = nouvelle ligne
                    
                    // On s'assure que le pion ne sort pas du plateau
                    if (nl >= 0 && nl < 8) {
                        
                        // Règle 1 : Mouvement TOUT DROIT
                        // Le pion peut avancer seulement si la case est vide (0)[cite: 36].
                        if (board[nl][c] == 0) {
                            mouvementsPossibles.add(new Coup(l, c, nl, c));
                        }
                        
                        // Règle 2 : Mouvement DIAGONALE GAUCHE
                        // Possible vers une case vide OU pour capturer un pion adverse[cite: 36, 37].
                        // "!= joueur" signifie que la case est soit vide (0), soit occupée par l'adversaire.
                        if (c - 1 >= 0 && board[nl][c - 1] != joueur) {
                            mouvementsPossibles.add(new Coup(l, c, nl, c - 1));
                        }
                        
                        // Règle 3 : Mouvement DIAGONALE DROITE
                        // Même logique que pour la diagonale gauche[cite: 36, 37].
                        if (c + 1 < 8 && board[nl][c + 1] != joueur) {
                            mouvementsPossibles.add(new Coup(l, c, nl, c + 1));
                        }
                    }
                }
            }
        }
        
        return mouvementsPossibles;
    }
}