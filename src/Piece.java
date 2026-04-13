/**
 * Représente le contenu d'une case du plateau.
 * Équivalent de Mark.java dans le TicTacToe géant.
 *
 * Protocole serveur : 0 = vide, 2 = noir, 4 = rouge
 */
public enum Piece {
    ROUGE,    // Pion rouge (valeur serveur : 4) — joue en premier, avance vers la rangée 8
    NOIR,  // Pion noir  (valeur serveur : 2) — avance vers la rangée 1
    VIDE   // Case vide  (valeur serveur : 0)
}
