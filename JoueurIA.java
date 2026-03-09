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

// NOUVEAU : Fonction d'évaluation statique
    public static int evaluerPlateau(int[][] board) {
        int score = 0;
        int nbRouges = 0;
        int nbNoirs = 0;

        // On parcourt tout le plateau
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                
                // Évaluation pour les pions ROUGES (Joueur 4)
                if (board[l][c] == 4) { 
                    // Condition de victoire absolue : le Rouge atteint la rangée 8 (indice 0)
                    if (l == 0) return 1000000;
                    
                    nbRouges++;
                    
                    // Bonus d'avancement :
                    // La ligne de départ est 7, l'arrivée est 0. 
                    // (7 - l) donne un multiplicateur qui augmente à mesure qu'il avance.
                    score += (7 - l) * 10; 
                } 
                
                // Évaluation pour les pions NOIRS (Joueur 2)
                else if (board[l][c] == 2) { 
                    // Condition de victoire absolue : le Noir atteint la rangée 1 (indice 7)
                    if (l == 7) return -1000000;
                    
                    nbNoirs++;
                    
                    // Bonus d'avancement :
                    // La ligne de départ est 0, l'arrivée est 7.
                    // l donne directement un multiplicateur qui augmente.
                    score -= l * 10;
                }
            }
        }

        // Avantage matériel : on donne un poids important au nombre de pièces (ex: 30 points par pion)
        score += (nbRouges - nbNoirs) * 30;

        return score;
    }
// 1. Outil pour copier le plateau lors des simulations
    public static int[][] copierPlateau(int[][] original) {
        int[][] copie = new int[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(original[i], 0, copie[i], 0, 8);
        }
        return copie;
    }

    // 2. L'algorithme Minimax avec élagage Alpha-Beta
    public static int minimax(int[][] plateau, int profondeur, int alpha, int beta, boolean maximisant, int maCouleur, int couleurAdversaire) {
        // Condition d'arrêt : on a atteint la limite de profondeur ou quelqu'un a gagné
        int score = evaluerPlateau(plateau);
        
        // Si le score est extrême (proche de 1000000 ou -1000000), c'est une victoire/défaite, on arrête.
        if (profondeur == 0 || Math.abs(score) >= 900000) {
            return score;
        }

        if (maximisant) {
            int maxEval = Integer.MIN_VALUE;
            ArrayList<Coup> mouvements = genererMouvements(plateau, maCouleur);
            
            for (Coup coup : mouvements) {
                // On simule le coup
                int[][] nouveauPlateau = copierPlateau(plateau);
                nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
                nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
                
                // On appelle Minimax récursivement pour le tour de l'adversaire
                int eval = minimax(nouveauPlateau, profondeur - 1, alpha, beta, false, maCouleur, couleurAdversaire);
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                // Élagage Alpha-Beta : on coupe la branche si ce n'est pas pertinent
                if (beta <= alpha) {
                    break; 
                }
            }
            return maxEval;
            
        } else {
            // Tour de l'adversaire (Minimisant)
            int minEval = Integer.MAX_VALUE;
            ArrayList<Coup> mouvements = genererMouvements(plateau, couleurAdversaire);
            
            for (Coup coup : mouvements) {
                int[][] nouveauPlateau = copierPlateau(plateau);
                nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
                nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
                
                int eval = minimax(nouveauPlateau, profondeur - 1, alpha, beta, true, maCouleur, couleurAdversaire);
                
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) {
                    break; 
                }
            }
            return minEval;
        }
    }

    // 3. La méthode principale que ton Client va appeler pour obtenir le coup à jouer
    public static Coup getMeilleurCoup(int[][] plateau, int profondeur, int maCouleur) {
        int couleurAdversaire = (maCouleur == 4) ? 2 : 4;
        int meilleurScore = Integer.MIN_VALUE;
        Coup meilleurCoup = null;
        
        ArrayList<Coup> mouvements = genererMouvements(plateau, maCouleur);
        
        for (Coup coup : mouvements) {
            // On simule notre coup
            int[][] nouveauPlateau = copierPlateau(plateau);
            nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
            nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
            
            // On lance l'algorithme pour évaluer ce coup (profondeur - 1, et c'est au tour de l'adversaire)
            int score = minimax(nouveauPlateau, profondeur - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, maCouleur, couleurAdversaire);
            
            // Si ce coup donne un meilleur score que les précédents, on le garde
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurCoup = coup;
            }
        }
        
        // Sécurité : si aucun coup n'est jugé "bon", on joue le premier coup légal par défaut
        if (meilleurCoup == null && !mouvements.isEmpty()) {
            meilleurCoup = mouvements.get(0);
        }
        
        return meilleurCoup;
    }
}