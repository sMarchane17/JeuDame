import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

/**
 * Client réseau pour le jeu de Breakthrough.
 *
 * Basé sur le template du professeur, adapté pour :
 *  - Utiliser l'IA (AIPlayer) au lieu de la saisie console
 *  - Parser le format de coup "D6-D5" (départ-arrivée)
 *  - Gérer le plateau 8x8 (64 valeurs)
 *  - Permettre de spécifier l'IP du serveur en argument ou via une boîte de dialogue
 *
 * Protocole serveur :
 *  '1' + 64 valeurs → Tu es ROUGE (joue en premier)
 *  '2' + 64 valeurs → Tu es NOIR  (attends le coup adverse)
 *  '3' + dernier coup adverse → C'est ton tour de jouer
 *  '4'              → Coup invalide, rejoue
 *  '5'              → Partie terminée
 */
public class Client {

    private static Board    globalBoard;   // Plateau 8x8
    private static AIPlayer ai;            // L'IA Alpha-Beta
    private static Piece    cpuPiece;      // Couleur de l'IA (RED ou BLACK)
    private static Piece    opponentPiece; // Couleur de l'adversaire

    public static void main(String[] args) {

        // ── Adresse IP du serveur ──────────────────────────────
        String serverIP = "localhost";
        if (args.length > 0) {
            serverIP = args[0];
        } else {
            String input = JOptionPane.showInputDialog(
                    null,
                    "Entrez l'adresse IP du serveur :",
                    "Configuration",
                    JOptionPane.QUESTION_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                serverIP = input.trim();
            }
        }
        System.out.println("Connexion à " + serverIP + ":8888 ...");

        Socket               myClient;
        BufferedInputStream  input;
        BufferedOutputStream output;

        try {
            myClient = new Socket(serverIP, 8888);
            input    = new BufferedInputStream(myClient.getInputStream());
            output   = new BufferedOutputStream(myClient.getOutputStream());

            globalBoard = new Board();

            // ── Boucle principale ──────────────────────────────
            while (true) {
                int cmdInt = input.read();
                if (cmdInt == -1) break; // connexion fermée

                char cmd = (char) cmdInt;
                System.out.println(">>> Commande reçue : " + cmd);

                // ── '1' : Nouvelle partie, je suis ROUGE ──────
                if (cmd == '1') {
                    readBoardState(input, globalBoard);
                    cpuPiece      = Piece.ROUGE;
                    opponentPiece = Piece.NOIR;
                    ai            = new AIPlayer(cpuPiece, 4700); // 4.7s (marge de sécurité)
                    System.out.println("Nouvelle partie — Je suis ROUGE");
                    globalBoard.printBoard();

                    // ROUGE joue en premier sans attendre de commande '3'
                    Move firstMove = ai.getBestMove(globalBoard);
                    if (firstMove == null) firstMove = new Move(7, 0, 6, 0); // coup de secours
                    globalBoard.play(firstMove);
                    String moveStr = firstMove.toString();
                    System.out.println("Premier coup : " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }

                // ── '2' : Nouvelle partie, je suis NOIR ───────
                else if (cmd == '2') {
                    readBoardState(input, globalBoard);
                    cpuPiece      = Piece.NOIR;
                    opponentPiece = Piece.ROUGE;
                    ai            = new AIPlayer(cpuPiece, 4700);
                    System.out.println("Nouvelle partie — Je suis NOIR");
                    globalBoard.printBoard();
                    // On attend que le serveur envoie '3' avant de jouer
                }

                // ── '3' : C'est mon tour ───────────────────────
                else if (cmd == '3') {
                    String lastMoveStr = readLineFromServer(input).trim();
                    System.out.println("Dernier coup adverse : " + lastMoveStr);

                    // Applique le coup adverse sur le plateau local
                    // (sauf le coup invalide initial "A8-A8" quand on est ROUGE)
                    if (!lastMoveStr.equals("A8-A8")) {
                        Move advMove = parseMove(lastMoveStr);
                        if (advMove != null) {
                            globalBoard.play(advMove);
                        }
                    }
                    globalBoard.printBoard();

                    // Calcule et envoie le meilleur coup
                    Move myMove = ai.getBestMove(globalBoard);
                    if (myMove == null) {
                        // Ne devrait jamais arriver, mais sécurité
                        System.out.println("[ERREUR] Aucun coup disponible !");
                        break;
                    }
                    globalBoard.play(myMove);
                    String moveStr = myMove.toString();
                    System.out.println("J'envoie : " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }

                // ── '4' : Coup invalide, rejouer ──────────────
                else if (cmd == '4') {
                    System.out.println("Coup invalide ! Je recalcule...");
                    Move myMove = ai.getBestMove(globalBoard);
                    if (myMove == null) myMove = new Move(7, 0, 6, 0);
                    globalBoard.play(myMove);
                    String moveStr = myMove.toString();
                    System.out.println("Nouveau coup : " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }

                // ── '5' : Partie terminée ─────────────────────
                else if (cmd == '5') {
                    String lastMoveStr = readLineFromServer(input).trim();
                    System.out.println("Partie terminée. Dernier coup : " + lastMoveStr);
                    globalBoard.printBoard();
                    Piece winner = globalBoard.checkWinner();
                    System.out.println("Gagnant : " + (winner == Piece.VIDE ? "Inconnu" : winner));
                    break;
                }
            }

            System.out.println("Fin de la connexion.");
            input.close();
            output.close();
            myClient.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Lecture de l'état initial du plateau ──────────────────────

    /**
     * Lit les 64 valeurs envoyées par le serveur après la commande '1' ou '2'
     * et initialise le plateau en conséquence.
     */
    private static void readBoardState(BufferedInputStream input, Board board)
            throws IOException {
        byte[] buffer = new byte[1024];
        int size = input.available();
        input.read(buffer, 0, size);
        String raw = new String(buffer).trim();
        System.out.println("État initial reçu : " + raw);
        String[] values = raw.split("\\s+");
        board.setFromServerData(values);
    }

    // ── Lecture d'une ligne courte (dernier coup) ──────────────────

    private static String readLineFromServer(BufferedInputStream input)
            throws IOException {
        byte[] buffer = new byte[32];
        int size = input.available();
        input.read(buffer, 0, size);
        return new String(buffer).trim();
    }

    // ── Conversion chaîne ↔ Move ───────────────────────────────────

    /**
     * Convertit la chaîne serveur en Move interne.
     * Formats acceptés : "D6-D5" ou "D6D5"
     *
     * Conversion rangée visuelle → index interne : internalRow = 8 - visualRow
     */
    private static Move parseMove(String moveStr) {
        if (moveStr == null || moveStr.length() < 4) return null;
        try {
            String from, to;
            if (moveStr.contains("-")) {
                String[] parts = moveStr.split("-");
                from = parts[0].trim();
                to   = parts[1].trim();
            } else {
                from = moveStr.substring(0, 2);
                to   = moveStr.substring(2, 4);
            }

            int fromCol = from.charAt(0) - 'A';
            int fromRow = 8 - (from.charAt(1) - '0'); // visuel → interne

            int toCol   = to.charAt(0) - 'A';
            int toRow   = 8 - (to.charAt(1) - '0');

            return new Move(fromRow, fromCol, toRow, toCol);

        } catch (Exception e) {
            System.err.println("Erreur de parsing du coup : " + moveStr);
            return null;
        }
    }
}
