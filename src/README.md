# Breakthrough - LOG320 Laboratoire

Implémentation du jeu de **Breakthrough** avec intelligence artificielle (algorithme Alpha-Beta).

---

## Structure du projet

```
Piece.java      → Enum des pièces (RED, BLACK, EMPTY)
Move.java       → Représentation d'un coup (départ + arrivée)
Board.java      → Plateau 8x8 et logique du jeu
AIPlayer.java   → Intelligence artificielle (Minimax + Alpha-Beta)
Client.java     → Connexion réseau au serveur de jeu
```

---

## Compilation

Dans le dossier contenant les fichiers `.java` :

```bash
cd src
javac Piece.java Move.java Board.java AIPlayer.java Client.java
```

Pour générer un `.jar` exécutable :

```bash
cd src
jar cfe Breakthrough.jar Client *.class
```

---

## Lancement - Jouer contre l'ordinateur (serveur local)

### Étape 1 - Lancer le serveur
1. Ouvre l'application **Breakthrough.jar** fournie par le professeur
2. Dans les options **Joueur 1 (Rouge)** → sélectionne **Réseau**
3. Dans les options **Joueur 2 (Noir)** → sélectionne **Ordinateur** et choisis le niveau (1, 2 ou 3)
4. Clique sur le bouton **★** pour démarrer la partie
5. Le serveur affiche `En attente du coup rouge...` - il est prêt

### Étape 2 - Lancer le client (ton IA)
```bash
# Option A - boîte de dialogue s'ouvre pour entrer l'IP
cd src
java Client

# Option B - IP directement en argument
cd src
java Client localhost
```

Quand la boîte de dialogue apparaît, entre :
```bash
localhost
```
> Le serveur et le client tournent sur la même machine, donc `localhost` suffit.

---

## Lancement - Jouer contre un autre client réseau (compétition)

### Étape 1 - Désigner la machine qui héberge le serveur
Un seul ordinateur fait tourner le serveur. Cette personne note son adresse IP :
```bash
# Windows
ipconfig
# Chercher "Adresse IPv4" sous le bon adaptateur réseau (Wi-Fi ou Ethernet)
# Exemple : 192.168.x.x
```

### Étape 2 - Configurer le serveur
1. Ouvre **Breakthrough.jar** (serveur du prof)
2. **Joueur 1 (Rouge)** → **Réseau**
3. **Joueur 2 (Noir)** → **Réseau**
4. Clique sur **★** pour démarrer
5. Le serveur attend maintenant deux connexions

### Étape 3 - Connecter les deux clients

> ⚠️ L'ordre est important : **le premier connecté = Rouge**, le second = Noir.

**Machine 1 - joueur Rouge (se connecte en premier) :**
```bash
cd src
java Client 192.168.x.x
# ou avec le .jar :
java -jar Breakthrough.jar 192.168.x.x
```

**Machine 2 - joueur Noir (se connecte en deuxième) :**
```bash
cd src
java Client 192.168.x.x
# ou avec le .jar :
java -jar Breakthrough.jar 192.168.x.x
```

Remplace `192.168.x.x` par l'IP réelle de la machine qui héberge le serveur.

---

## Protocole serveur (référence rapide)

| Commande reçue | Signification |
|---|---|
| `1` + 64 valeurs | Nouvelle partie, tu es **Rouge** (joue en premier) |
| `2` + 64 valeurs | Nouvelle partie, tu es **Noir** (attends le coup adverse) |
| `3` + dernier coup | C'est **ton tour** de jouer |
| `4` | Coup invalide, **rejoue** |
| `5` | **Partie terminée** |

Représentation des pièces :

| Valeur | Pièce |
|---|---|
| `0` | Case vide |
| `2` | Pion noir |
| `4` | Pion rouge |

Format d'un coup envoyé au serveur :
```
D6-D5   (colonne + rangée de départ) - (colonne + rangée d'arrivée)
```

---

## Règles du jeu (rappel)

- Plateau **8×8**, chaque joueur commence avec **16 pions** (2 premières rangées)
- Les **Rouges** jouent en premier et avancent vers la **rangée 8**
- Les **Noirs** avancent vers la **rangée 1**
- Déplacements possibles (toujours vers l'avant) :
  - **Tout droit** : uniquement si la case est vide
  - **Diagonale gauche/droite** : si la case est vide ou occupée par un ennemi (capture)
- **Aucun recul** possible
- **Victoire** : premier joueur à atteindre la rangée adverse

---

## Intelligence artificielle

L'IA utilise l'algorithme **Alpha-Beta** avec **approfondissement itératif** :
- Explore progressivement des profondeurs croissantes (1, 2, 3, ...)
- S'arrête automatiquement avant la limite de **5 secondes**
- Conserve toujours le meilleur coup de la dernière profondeur complète

Critères d'évaluation (par ordre d'importance) :
1. **Matériel** - nombre de pièces restantes
2. **Avancement** - progression vers la zone adverse
3. **Menace de victoire** - pièce à 1 case du but
4. **Protection** - pièce couverte par une alliée