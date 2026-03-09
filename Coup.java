// Coup.java
public class Coup {
    public int ligneDepart;
    public int colonneDepart;
    public int ligneArrivee;
    public int colonneArrivee;

    // Constructeur
    public Coup(int ligneDepart, int colonneDepart, int ligneArrivee, int colonneArrivee) {
        this.ligneDepart = ligneDepart;
        this.colonneDepart = colonneDepart;
        this.ligneArrivee = ligneArrivee;
        this.colonneArrivee = colonneArrivee;
    }

    // Convertit le coup au format texte attendu par le serveur (ex: "A2-A3")
    public String versServeur() {
        char colDepartChar = (char) ('A' + colonneDepart);
        char colArriveeChar = (char) ('A' + colonneArrivee);
        
        // On reconvertit les indices (0-7) en vraies lignes du plateau (8-1)
        int ligneDepartVraie = 8 - ligneDepart;
        int ligneArriveeVraie = 8 - ligneArrivee;
        
        return "" + colDepartChar + ligneDepartVraie + "-" + colArriveeChar + ligneArriveeVraie;
    }
}