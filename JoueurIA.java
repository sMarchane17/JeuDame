import java.util.ArrayList;

public class JoueurIA {

    private static long tempsDebut;
    private static boolean tempsEcoule;
    private static final long TEMPS_MAX = 4500;

    public static ArrayList<Coup> genererMouvements(int[][] board, int joueur) {
        ArrayList<Coup> mouvementsPossibles = new ArrayList<>(40);
        int direction = (joueur == 4) ? -1 : 1;
        int ligneVictoire = (joueur == 4) ? 0 : 7;
        
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                if (board[l][c] == joueur) {
                    int nl = l + direction;
                    if (nl >= 0 && nl < 8) {
                        
                        // Règle 1 : aller tout droit
                        if (board[nl][c] == 0) {
                            Coup coupDroit = new Coup(l, c, nl, c);
                            if (nl == ligneVictoire) {
                                ArrayList<Coup> win = new ArrayList<>(); win.add(coupDroit); return win; 
                            }
                            mouvementsPossibles.add(coupDroit);
                        }
                        
                        // Règle 2 : aller en diagonale gauche
                        if (c - 1 >= 0 && board[nl][c - 1] != joueur) {
                            Coup coupGauche = new Coup(l, c, nl, c - 1);
                            if (nl == ligneVictoire) {
                                ArrayList<Coup> win = new ArrayList<>(); win.add(coupGauche); return win; 
                            }
                            if (board[nl][c - 1] != 0) mouvementsPossibles.add(0, coupGauche); 
                            else mouvementsPossibles.add(coupGauche);
                        }
                        
                        // Règle 3 : aller en diagonale droite
                        if (c + 1 < 8 && board[nl][c + 1] != joueur) {
                            Coup coupDroitDiag = new Coup(l, c, nl, c + 1);
                            if (nl == ligneVictoire) {
                                ArrayList<Coup> win = new ArrayList<>(); win.add(coupDroitDiag); return win; 
                            }
                            if (board[nl][c + 1] != 0) mouvementsPossibles.add(0, coupDroitDiag); 
                            else mouvementsPossibles.add(coupDroitDiag);
                        }
                    }
                }
            }
        }
        return mouvementsPossibles;
    }

    // Évaluation de l'état du plateau
    public static int evaluerPlateau(int[][] board, int maCouleur) {
        int score = 0;
        int couleurAdversaire = (maCouleur == 4) ? 2 : 4;

        int monMaxAvancement = 0;
        int advMaxAvancement = 0;
        int nbMoi = 0;
        int nbAdversaire = 0;

        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                
                // --- mes pions ---
                if (board[l][c] == maCouleur) { 
                    if (maCouleur == 4 && l == 0) return 1000000;
                    if (maCouleur == 2 && l == 7) return 1000000;
                    
                    nbMoi++;
                    int avancee = (maCouleur == 4) ? (7 - l) : l;
                    monMaxAvancement = Math.max(monMaxAvancement, avancee);
                    
                    score += (avancee * avancee) * 5; 
                    if (c >= 2 && c <= 5) score += 10; // Contrôle du centre
                    
                    // Système offensif
                    if (avancee == 6) score += 50000; // Je suis à 1 case de gagner.
                    
                    // Je garde des pions sur ma première ligne pour bloquer
                    if (maCouleur == 4 && l == 7) score += 20;
                    if (maCouleur == 2 && l == 0) score += 20;
                    
                    // couverture toujours d'un pion
                    if (maCouleur == 4 && l < 7) {
                        if (c > 0 && board[l+1][c-1] == maCouleur) score += 15;
                        if (c < 7 && board[l+1][c+1] == maCouleur) score += 15;
                    } else if (maCouleur == 2 && l > 0) {
                        if (c > 0 && board[l-1][c-1] == maCouleur) score += 15;
                        if (c < 7 && board[l-1][c+1] == maCouleur) score += 15;
                    }
                } 
                
                // --- pions adversaire ---
                else if (board[l][c] == couleurAdversaire) { 
                    if (couleurAdversaire == 4 && l == 0) return -1000000;
                    if (couleurAdversaire == 2 && l == 7) return -1000000;
                    
                    nbAdversaire++;
                    int avancee = (couleurAdversaire == 4) ? (7 - l) : l;
                    advMaxAvancement = Math.max(advMaxAvancement, avancee);
                    
                    score -= (avancee * avancee) * 5;
                    if (c >= 2 && c <= 5) score -= 10;
                    
                    // Système défensif
                    if (avancee == 6) score -= 50000; // Il est à 1 case de gagner
                    
                    // couverture adversaire
                    if (couleurAdversaire == 4 && l < 7) {
                        if (c > 0 && board[l+1][c-1] == couleurAdversaire) score -= 15;
                        if (c < 7 && board[l+1][c+1] == couleurAdversaire) score -= 15;
                    } else if (couleurAdversaire == 2 && l > 0) {
                        if (c > 0 && board[l-1][c-1] == couleurAdversaire) score -= 15;
                        if (c < 7 && board[l-1][c+1] == couleurAdversaire) score -= 15;
                    }
                }
            }
        }

        if (monMaxAvancement > advMaxAvancement) score += 100;
        else if (advMaxAvancement > monMaxAvancement) score -= 100;

        score += (nbMoi - nbAdversaire) * 30;

        return score;
    }

    public static int[][] copierPlateau(int[][] original) {
        int[][] copie = new int[8][8];
        for (int i = 0; i < 8; i++) System.arraycopy(original[i], 0, copie[i], 0, 8);
        return copie;
    }

    public static int minimax(int[][] plateau, int profondeur, int alpha, int beta, boolean maximisant, int maCouleur, int couleurAdversaire) {
        if (System.currentTimeMillis() - tempsDebut > TEMPS_MAX) {
            tempsEcoule = true;
            return evaluerPlateau(plateau, maCouleur);
        }

        int score = evaluerPlateau(plateau, maCouleur);
        if (profondeur == 0 || Math.abs(score) >= 900000) return score;

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
                if (beta <= alpha) break; 
            }
            return maxEval;
            
        } else {
            int minEval = Integer.MAX_VALUE;
            ArrayList<Coup> mouvements = genererMouvements(plateau, couleurAdversaire);
            
            for (Coup coup : mouvements) {
                int[][] nouveauPlateau = copierPlateau(plateau);
                nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
                nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
                
                int eval = minimax(nouveauPlateau, profondeur - 1, alpha, beta, true, maCouleur, couleurAdversaire);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; 
            }
            return minEval;
        }
    }

    public static Coup getMeilleurCoup(int[][] plateau, int profondeurDemande, int maCouleur) {
        tempsDebut = System.currentTimeMillis(); 
        tempsEcoule = false;
        
        int couleurAdversaire = (maCouleur == 4) ? 2 : 4;
        ArrayList<Coup> mouvements = genererMouvements(plateau, maCouleur);
        
        if (mouvements.isEmpty()) return null; 
        
        Coup meilleurCoupAbsolu = mouvements.get(0); 
        int meilleurScoreGlobal = Integer.MIN_VALUE;
        
        for (int profondeur = 1; profondeur <= 20; profondeur++) {
            int meilleurScoreProfondeur = Integer.MIN_VALUE;
            Coup meilleurCoupProfondeur = null;
            
            for (Coup coup : mouvements) {
                if (tempsEcoule) break; 
                
                int[][] nouveauPlateau = copierPlateau(plateau);
                nouveauPlateau[coup.ligneArrivee][coup.colonneArrivee] = nouveauPlateau[coup.ligneDepart][coup.colonneDepart];
                nouveauPlateau[coup.ligneDepart][coup.colonneDepart] = 0;
                
                int score = minimax(nouveauPlateau, profondeur - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, maCouleur, couleurAdversaire);
                
                if (score > meilleurScoreProfondeur && !tempsEcoule) {
                    meilleurScoreProfondeur = score;
                    meilleurCoupProfondeur = coup;
                }
            }
            
            if (tempsEcoule) {
                System.out.println("-> Temps limite (4.5s) atteint. Profondeur complétée : " + (profondeur - 1));
                break; 
            }
            
            if (meilleurCoupProfondeur != null) {
                meilleurCoupAbsolu = meilleurCoupProfondeur;
                meilleurScoreGlobal = meilleurScoreProfondeur;
            }
            
            if (meilleurScoreGlobal >= 900000) break;
        }
        
        return meilleurCoupAbsolu;
    }
}