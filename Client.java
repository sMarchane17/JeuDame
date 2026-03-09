import java.io.*;
import java.net.*;

class Client {

    public static void afficherPlateau(int[][] board) {
        System.out.println("\n  A B C D E F G H");
        for (int i = 0; i < 8; i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 0) System.out.print(". "); 
                else if (board[i][j] == 2) System.out.print("N "); 
                else if (board[i][j] == 4) System.out.print("R "); 
            }
            System.out.println(" " + (8 - i));
        }
        System.out.println("  A B C D E F G H\n");
    }

    public static void appliquerCoup(int[][] board, String move) {
        if (move == null || move.isEmpty()) return;
        move = move.replace("-", "").replace(" ", "").trim();
        
        if (move.length() >= 4) {
            int startCol = move.charAt(0) - 'A';
            int startRow = 8 - Character.getNumericValue(move.charAt(1));
            int endCol = move.charAt(2) - 'A';
            int endRow = 8 - Character.getNumericValue(move.charAt(3));
            
            if (startRow >= 0 && startRow < 8 && startCol >= 0 && startCol < 8 &&
                endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) {
                
                if (startRow != endRow || startCol != endCol) {
                    board[endRow][endCol] = board[startRow][startCol];
                    board[startRow][startCol] = 0;
                }
            }
        }
    }

    public static void main(String[] args) {
         
    Socket MyClient;
    BufferedInputStream input;
    BufferedOutputStream output;
        int[][] board = new int[8][8];
        int maCouleur = 4; // On retient notre couleur (4 pour Rouge, 2 pour Noir)
    
    try {
        MyClient = new Socket("localhost", 8888);

        input    = new BufferedInputStream(MyClient.getInputStream());
        output   = new BufferedOutputStream(MyClient.getOutputStream());
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        
        while(1 == 1){
            char cmd = 0;
            
            cmd = (char)input.read();
            System.out.println(cmd);
            
            // Debut de la partie en joueur blanc (Rouge)
            if(cmd == '1'){
                maCouleur = 4; // Je suis Rouge
                byte[] aBuffer = new byte[1024];
                
                int size = input.available();
                input.read(aBuffer,0,size);
                String s = new String(aBuffer).trim();
                System.out.println(s);
                String[] boardValues = s.split(" ");
                int x=0,y=0;
                for(int i=0; i<boardValues.length;i++){
                    board[y][x] = Integer.parseInt(boardValues[i]);
                    x++;
                    if(x == 8){
                        x = 0;
                        y++;
                    }
                }

                afficherPlateau(board);

                System.out.println("L'IA (Rouge) réfléchit à son premier coup...");
                // Appel à l'IA avec une profondeur de 5
                Coup meilleurCoup = JoueurIA.getMeilleurCoup(board, 5, maCouleur);
                String move = meilleurCoup.versServeur();
                System.out.println("L'IA a choisi : " + move);
                
                appliquerCoup(board, move);
                
                output.write(move.getBytes(),0,move.length());
                output.flush();
            }
            
            // Debut de la partie en joueur Noir
            if(cmd == '2'){
                maCouleur = 2; // Je suis Noir
                System.out.println("Nouvelle partie! Vous jouez noir, attendez le coup des blancs");
                byte[] aBuffer = new byte[1024];
                
                int size = input.available();
                input.read(aBuffer,0,size);
                String s = new String(aBuffer).trim();
                System.out.println(s);
                String[] boardValues = s.split(" ");
                int x=0,y=0;
                for(int i=0; i<boardValues.length;i++){
                    board[y][x] = Integer.parseInt(boardValues[i]);
                    x++;
                    if(x == 8){
                        x = 0;
                        y++;
                    }
                }
                afficherPlateau(board);
            }

            // Le serveur demande le prochain coup
            if(cmd == '3'){
                byte[] aBuffer = new byte[16];
                
                int size = input.available();
                input.read(aBuffer,0,size);
                
                String s = new String(aBuffer).trim();
                System.out.println("\n--- TOUR DE JEU ---");
                System.out.println("Le serveur a joué : " + s);
                
                appliquerCoup(board, s);
                afficherPlateau(board);
                
                System.out.println("L'IA réfléchit...");
                // Appel à l'IA avec une profondeur de 5
                Coup meilleurCoup = JoueurIA.getMeilleurCoup(board, 5, maCouleur);
                String move = meilleurCoup.versServeur();
                System.out.println("L'IA a choisi : " + move);
                
                appliquerCoup(board, move);
                
                output.write(move.getBytes(),0,move.length());
                output.flush();
            }
            
            // Le dernier coup est invalide
            if(cmd == '4'){
                System.out.println("Coup invalide, le serveur demande un nouveau coup.");
                // Si l'IA fait une erreur, on lui redemande de calculer (ne devrait pas arriver avec un bon générateur)
                Coup meilleurCoup = JoueurIA.getMeilleurCoup(board, 5, maCouleur);
                String move = meilleurCoup.versServeur();
                System.out.println("L'IA tente ce nouveau coup : " + move);
                
                appliquerCoup(board, move);
                output.write(move.getBytes(),0,move.length());
                output.flush();
            }
            
            // La partie est terminée
            if(cmd == '5'){
                byte[] aBuffer = new byte[16];
                int size = input.available();
                input.read(aBuffer,0,size);
                String s = new String(aBuffer).trim();
                System.out.println("Partie Terminée. Le dernier coup joué est: " + s);
                appliquerCoup(board, s);
                afficherPlateau(board);
                
                // On peut juste envoyer "OK" ou fermer la connexion ici
                String move = "OK\n";
                output.write(move.getBytes(),0,move.length());
                output.flush();
            }
        }
    }
    catch (IOException e) {
        System.out.println(e);
    }
    
    }
}