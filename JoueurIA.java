import java.util.ArrayList;

public class JoueurIA {

    // ==========================================
    // VARIABLES POUR LE CHRONOMÈTRE
    // ==========================================
    private static long tempsDebut;
    private static boolean tempsEcoule;
    private static final long TEMPS_MAX = 4500; // 4.5 secondes de temps de réflexion maximum

    // 1. Méthode qui génère tous les coups possibles pour un joueur donné
    public static ArrayList<Coup> genererMouvements(int[][] board, int joueur) {
        ArrayList<Coup> mouvementsPossibles = new ArrayList<>(40);
        
        int direction = (joueur == 4) ? -1 : 1;
        
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                
                if (board[l][c] == joueur) {
                    int nl = l + direction;
                    
                    if (nl >= 0 && nl < 8) {
                        
                        // Règle 1 : Mouvement TOUT DROIT
                        if (board[nl][c] == 0) {
                            mouvementsPossibles.add(new Coup(l, c, nl, c));
                        }
                        
                        // Règle 2 : Mouvement DIAGONALE GAUCHE
                        if (c - 1 >= 0 && board[nl][c - 1] != joueur) {
                            mouvementsPossibles.add(new Coup(l, c, nl, c - 1));
                        }
                        
                        // Règle 3 : Mouvement DIAGONALE DROITE
                        if (c + 1 < 8 && board[nl][c + 1] != joueur) {
                            mouvementsPossibles.add(new Coup(l, c, nl, c + 1));
                        }
                    }
                }
            }
        }
        
        return mouvementsPossibles;
    }

    // 2. Fonction d'évaluation avec le système de score exponentiel
    public static int evaluerPlateau(int[][] board, int maCouleur) {
        int score = 0;
        int nbMoi = 0;
        int nbAdversaire = 0;
        int couleurAdversaire = (maCouleur == 4) ? 2 : 4;

        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                
                // Si c'est MON pion (Positif)
                if (board[l][c] == maCouleur) { 
                    if (maCouleur == 4 && l == 0) return 1000000;
                    if (maCouleur == 2 && l == 7) return 1000000;
                    
                    nbMoi++;
                    // Bonus d'avancement exponentiel (au carré)
                    if (maCouleur == 4) {
                        int avancee = 7 - l;
                        score += (avancee * avancee) * 5; 
                    }
                    if (maCouleur == 2) {
                        int avancee = l;
                        score += (avancee * avancee) * 5; 
                    }
                } 
                
                // Si c'est le pion de l'ADVERSAIRE (Négatif)
                else if (board[l][c] == couleurAdversaire) { 
                    if (couleurAdversaire == 4 && l == 0) return -1000000;
                    if (couleurAdversaire == 2 && l == 7) return -1000000;
                    
                    nbAdversaire++;
                    // Malus d'avancement de l'adversaire exponentiel
                    if (couleurAdversaire == 4) {
                        int avancee = 7 - l;
                        score -= (avancee * avancee) * 5;
                    }
                    if (couleurAdversaire == 2) {
                        int avancee = l;
                        score -= (avancee * avancee) * 5;
                    }
                }
            }
        }

        // Avantage matériel : Mes pions moins ses pions
        score += (nbMoi - nbAdversaire) * 30;

        return score;
    }

    // 3. Outil pour copier le plateau lors des simulations
    public static int[][] copierPlateau(int[][] original) {
        int[][] copie = new int[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(original[i], 0, copie[i], 0, 8);
        }
        return copie;
    }

    // 4. L'algorithme Minimax avec élagage Alpha-Beta et contrôle du temps
    public static int minimax(int[][] plateau, int profondeur, int alpha, int beta, boolean maximisant, int maCouleur, int couleurAdversaire) {
        
        // VÉRIFICATION DU TEMPS : On coupe tout si on dépasse 4.5s
        if (System.currentTimeMillis() - tempsDebut > TEMPS_MAX) {
            tempsEcoule = true;
            return evaluerPlateau(plateau, maCouleur);
        }

        int score = evaluerPlateau(plateau, maCouleur);
        
        if (profondeur == 0 || Math.abs(score) >= 900000) {
            return score;
        }

        if (maximisant) {
            int maxEval = Integer.MIN_VALUE;
            ArrayList<Coup> mouvements = genererMouvements(plateau, maCouleur);
            
            for (Coup coup : mouvements) {
                int[][] nouveauPlateau = copierPlateau(plateau);
                nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
                nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
                
                int eval = minimax(nouveauPlateau, profondeur - 1, alpha, beta, false, maCouleur, couleurAdversaire);
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
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

    // 5. La méthode principale (Chef d'Orchestre) avec Iterative Deepening
    public static Coup getMeilleurCoup(int[][] plateau, int profondeurDemande, int maCouleur) {
        tempsDebut = System.currentTimeMillis(); // On démarre le chrono
        tempsEcoule = false;
        
        int couleurAdversaire = (maCouleur == 4) ? 2 : 4;
        ArrayList<Coup> mouvements = genererMouvements(plateau, maCouleur);
        
        // Sécurité anti-crash au cas où la liste serait vide
        if (mouvements.isEmpty()) return null; 
        
        Coup meilleurCoupAbsolu = mouvements.get(0); 
        int meilleurScoreGlobal = Integer.MIN_VALUE;
        
        // Boucle d'exploration progressive (Iterative Deepening)
        for (int profondeur = 1; profondeur <= 20; profondeur++) {
            int meilleurScoreProfondeur = Integer.MIN_VALUE;
            Coup meilleurCoupProfondeur = null;
            
            for (Coup coup : mouvements) {
                if (tempsEcoule) break; // Arrêt d'urgence si le temps est écoulé
                
                int[][] nouveauPlateau = copierPlateau(plateau);
                nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
                nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
                
                int score = minimax(nouveauPlateau, profondeur - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, maCouleur, couleurAdversaire);
                
                if (score > meilleurScoreProfondeur && !tempsEcoule) {
                    meilleurScoreProfondeur = score;
                    meilleurCoupProfondeur = coup;
                }
            }
            
            // Si le chrono a sonné pendant la recherche de cette profondeur, on l'annule
            if (tempsEcoule) {
                System.out.println("-> Temps limite de 4.5s atteint. Profondeur complétée : " + (profondeur - 1));
                break; 
            }
            
            // Si on a fini la profondeur à temps, on met à jour notre choix officiel
            if (meilleurCoupProfondeur != null) {
                meilleurCoupAbsolu = meilleurCoupProfondeur;
                meilleurScoreGlobal = meilleurScoreProfondeur;
            }
            
            // Si on trouve un coup qui garantit la victoire absolue, on arrête de chercher
            if (meilleurScoreGlobal >= 900000) {
                break;
            }
        }
        
        return meilleurCoupAbsolu;
    }
}