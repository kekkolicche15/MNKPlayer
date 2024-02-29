### Descrizione di SufferingDoge

SufferingDoge è un bot progettato per giocare a diverse configurazioni di MNK, una generalizzazione del gioco del tic-tac-toe. In questa variante, **M** rappresenta il numero di righe, **N** il numero di colonne e **K** il numero di simboli che il giocatore deve concatenare per vincere.

Il funzionamento di SufferingDoge si basa sull'algoritmo MiniMax con potatura AlphaBeta. Per aumentare l'efficienza dell'esplorazione dell'albero di gioco, sono state introdotte due euristiche.

Quando vengono generati i figli diretti di uno stato della scacchiera, essi vengono valutati da una prima euristica che considera esclusivamente lo stato delle celle attorno a quella piazzata. Successivamente, vengono ordinati in base a quanto sono promettenti. Di conseguenza, i figli più promettenti vengono esaminati prima nell'albero di gioco, mentre quelli meno promettenti sono esaminati successivamente.

Se, nonostante gli accorgimenti precedenti, non si riesce ad arrivare ad uno stato finale di gioco (vittoria, pareggio, sconfitta), viene adottata un'altra euristica che valuta lo stato globale della scacchiera, assegnandole un punteggio.

Per compilare il progetto, è necessario eseguire il seguente comando mentre si è nella cartella `SufferingDoge`:
```bash
javac -d "./bins" **/*.java
```
Sono possibili partite nelle seguenti configurazioni:
- Giocatore vs Giocatore
	Eseguire
	 ```bash
	java -cp "./bins" mnkgame.MNKGame <M> <N> <K>
	```
- Giocatore vs Bot
	Eseguire
	 ```bash
	java -cp "./bins" mnkgame.MNKGame <M> <N> <K> <bot>
	```
- Bot vs Bot
	  Eseguire
	 ```bash
	java -cp "./bins" mnkgame.MNKGame <M> <N> <K> <bot1> <bot2>
	```
I bot disponibili sono:
- ``mnkgame.RandomPlayer``: effettua mosse casuali;
- ``mnkgame.QuasiRandomPlayer``: effettua mosse casuali, ma se manca una sola mossa alla vittoria o alla sconfitta, effettua la mossa corretta;
- ``SufferingDoge.SufferingDoge``: utilizza MiniMax con potatura AlphaBeta.

Qualora si volessero eseguire piu' confronti tra bot sulla singola configurazione e' possibile eseguire:
```bash
java -cp "./bins" mnkgame.MNKPlayerTester
```

SufferingDoge e' il risultato del progetto di Algoritmi e strutture dati @unibo A.Y. 2021/2022



