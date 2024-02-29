package SufferingDoge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

public class Board extends MNKBoard {
    public static final int[][] dirs = { { 0, 1 }, { 1, 0 }, { 1, 1 }, { 1, -1 } };

    private HashMap<Integer, MNKCell> adjMap;
    private int[][][][] seqBoard;
    private int[][] seqCount;
    private Deque<Integer[][][]> seqStack;
    private Deque<Integer[][]> countStack;

    /** 
     * @cost O(M*N)
     */
    public Board(int M, int N, int K) throws IllegalArgumentException {
        super(M, N, K);
        adjMap = new HashMap<>(M * N);
        seqBoard = new int[2][4][M][N];
        seqCount = new int[2][K];
        seqStack = new ArrayDeque<>();
        countStack = new ArrayDeque<>();
    }

    /** 
     * @cost O(1)
     */
    private boolean isValid(Coord c) {
        return isValid(c.i, c.j);
    }
    /** 
     * @cost O(1)
     */
    private boolean isValid(int i, int j) {
        return i >= 0 && i < M && j >= 0 && j < N;
    }

    /** 
     * @cost O(1)
     */
    private boolean isMarked(int i, int j) {
        return isValid(i, j) && cellState(i, j) != MNKCellState.FREE;
    }

    /** 
     * @cost O(1)
     */
    private MNKCellState opp(MNKCellState s) {
        return s == SufferingDoge.me ? SufferingDoge.opp : SufferingDoge.me;
    }

    /** 
     * @cost O(M*N)
     */
    public MNKCell[] getAdjacents() {
        return adjMap.values().toArray(new MNKCell[adjMap.size()]);
    }
    /** 
     * @cost O(1)
     */
    public boolean isAdjacent(MNKCell c) {
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                if (isMarked(c.i + i, c.j + j))
                    return true;
        return false;
    }
    /** 
     * Data una MMNKCell c restituisce la lista di celle adiacenti a c che non sono marcate
     * @cost O(1)
     */
    private List<MNKCell> getAdjacents(MNKCell c) {
        ArrayList<MNKCell> adj = new ArrayList<>();
        for (int i = c.i - 1; i <= c.i + 1; i++)
            for (int j = c.j - 1; j <= c.j + 1; j++)
                if (isValid(i, j) && !isMarked(i, j) && !(i == c.i && j == c.j))
                    adj.add(new MNKCell(i, j));
        return adj;
    }

    /** 
     * @cost O(K)
     */
    private Integer[] getVect(int[][] mat, Coord c, Coord dir) {
        Integer[] res = new Integer[2 * K + 1];
        Coord bw = new Coord(c).sub(dir);
        Coord fw = new Coord(c);
        for(int k=1; k<=K; k++, bw.sub(dir))
            if(isValid(bw))
                res[K-k] = mat[bw.i][bw.j];
            else
                break;
        for(int k=0; k<=K; k++, fw.add(dir))
            if(isValid(fw))
                res[K+k] = mat[fw.i][fw.j];
            else
                break;
        return res;
    }
    /** Salvo in seqStack le celle che potrebbero variare in seguito alla selezione della cella c.
     *   Inoltre salvo in countStack due vettori di interi che rappresentano i contatori delle sequenze
     * @cost O(K)
     */
    private void saveState(MNKCell c) {
        Integer[][][] table = new Integer[2][4][2 * K + 1];
        for (int p = 0; p < 2; p++)
            for (int d = 0; d < 4; d++)
                table[p][d] = getVect(seqBoard[p][d], new Coord(c), new Coord(dirs[d][0], dirs[d][1]));
        seqStack.addFirst(table);
        Integer[][] sq = new Integer[2][K];
        for(int p = 0; p < 2; p++)
            for(int k = 0; k < K; k++)
                sq[p][k] = seqCount[p][k];
        countStack.addFirst(sq);
    }
    /** 
     * Riporto lo stato dei contatori delle sequenze a prima della selezione della cella c
     * @cost O(K)
     */
    private void restoreState(MNKCell c) {
        Integer[][][] table = seqStack.removeFirst();
        for (int p = 0; p < 2; p++)
            for (int d = 0; d < 4; d++)
                for (int k = 0; k < table[p][d].length; k++)
                    if (isValid(c.i + dirs[d][0] * (k - K), c.j + dirs[d][1] * (k - K)))
                        seqBoard[p][d][c.i + dirs[d][0] * (k - K)][c.j + dirs[d][1] * (k - K)] = table[p][d][k];
        Integer[][] sq = countStack.removeFirst();
        for(int p = 0; p < 2; p++)
            for(int k = 0; k < K; k++)
                seqCount[p][k] = sq[p][k];
    }
    /** 
     * @cost O(1)
     */
    public void removeLastSeq() {
        seqStack.removeLast();
        countStack.removeLast();
    }

    /** 
     * @cost O(K^2)
     */
    @Override
    public MNKGameState markCell(int i, int j) throws IndexOutOfBoundsException, IllegalStateException {
        return markCell(new MNKCell(i, j));
    }
    /** 
     * Salvo lo stato delle sequenze e dei contatori delle sequenze;
     * Aggiorno lo stato della cella c;
     * Aggiorno l'insieme delle celle adiacenti, lo stato delle sequenze e dei contatori delle sequenze;
     * @cost O(K^2)
     */
    public MNKGameState markCell(MNKCell c) throws IndexOutOfBoundsException, IllegalStateException {
        saveState(new MNKCell(c.i, c.j));
        MNKGameState state = super.markCell(c.i, c.j);
        adjMap.remove(c.i*N+c.j);
        for (MNKCell adj : getAdjacents(c))
            adjMap.put(adj.i*N+adj.j, adj);
        for (int p = 0; p < 2; p++)
            for (int d = 0; d < 4; d++)
                updateSequencesInDir(seqBoard[p][d], cellState(c.i, c.j), new Coord(c),
                        new Coord(dirs[d][0], dirs[d][1]), p == 0 ? SufferingDoge.me : SufferingDoge.opp);
        return state;
    }

    /** 
     * Riporto lo stato delle celle adiacenti, delle sequenze e dei contatori delle sequenze a prima della selezione della cella c;
     * @cost O(K)
     */
    @Override
    public void unmarkCell() throws IllegalStateException {
        MNKCell tmp = MC.getLast();
        MNKCell last = new MNKCell(tmp.i, tmp.j);
        ArrayList<MNKCell> toCheck = (ArrayList<MNKCell>) getAdjacents(last);
        toCheck.add(last);
        for (MNKCell adj : toCheck)
            adjMap.remove(adj.i*N+adj.j);
        super.unmarkCell();
        for (MNKCell c : toCheck)
            if (isAdjacent(c))
                adjMap.put(c.i*N+c.j, c);
        restoreState(last);
    }

    /** 
     * Data una direzione, calcola per ogni cella considerata la dimensione della piu' lunga sequenza che la coinvolge.
     * Complementare di calcVectEndToStart.
     * @cost O(K^2)
     */
    private void calcVectEndToStart(int[] s, int startVect, int endVect, Coord endMat, Coord dir, MNKCellState mark){
        Coord iter = new Coord(endMat);
        for(int i=endVect; i>=startVect; i--, iter.sub(dir)){
            if(i == endVect){
                s[i] = 0;
                Coord tmp = new Coord(endMat);
                for(int j=0; j<K && isValid(tmp); j++, tmp.sub(dir))
                    s[i] += B[tmp.i][tmp.j]==MNKCellState.FREE?0:1;
            }
            else if(!isValid(iter.nsub(dir.nmul(K-1))) || B[iter.i-(K-1)*dir.i][iter.j-(K-1)*dir.j] == opp(mark)){
                int count = 0;
                while(i-count >= startVect)
                    s[i-count++] = s[i+1];
                break;
            }
            else{
                int possibleSol = s[endVect];
                int delta = endVect-i;
                Coord remove = new Coord(endMat);
                for(int j=0; j<delta; j++, remove.sub(dir)){
                    Coord add = new Coord(remove).sub(dir.nmul(K));
                    possibleSol -= B[remove.i][remove.j]==MNKCellState.FREE?0:1;
                    possibleSol += B[add.i][add.j]==MNKCellState.FREE?0:1;
                }
                s[i] = Math.max(possibleSol, s[i+1]);
            }
        }
    }
    /** 
     * Data una direzione, calcola per ogni cella considerata la dimensione della piu' lunga sequenza che la coinvolge.
     * Complementare di calcVectStartToEnd.
     * @cost O(K^2)
     */
    private void calcVectStartToEnd(int[] s, int startVect, int endVect, Coord startMat, Coord dir, MNKCellState mark){
        Coord iter = new Coord(startMat);
        for(int i=startVect; i<=endVect; i++, iter.add(dir)){
            if(i == startVect){
                s[i] = 0;
                Coord tmp = new Coord(startMat);
                for(int j=0; j<K && isValid(tmp); j++, tmp.add(dir))
                    s[i] += B[tmp.i][tmp.j]==MNKCellState.FREE?0:1;
            }
            else if(!isValid(iter.nadd(dir.nmul(K-1)))||B[iter.i+(K-1)*dir.i][iter.j+(K-1)*dir.j] == opp(mark)){
                int count = 0;
                while(i+count <= endVect)
                    s[i+count++] = s[i-1];
                break;
            }
            else{
                int possibleSol = s[startVect];
                int delta = i-startVect;
                Coord remove = new Coord(startMat);
                for(int j=0; j<delta; j++, remove.add(dir)){
                    Coord add = new Coord(remove).add(dir.nmul(K));
                    possibleSol -= B[remove.i][remove.j]==MNKCellState.FREE?0:1;
                    possibleSol += B[add.i][add.j]==MNKCellState.FREE?0:1;
                }
                s[i] = Math.max(possibleSol, s[i-1]);
            }
        }
    }
    /**
     * Data una cella e una direzione, aggiorna le matrici delle sequenze e dei contatori delle sequenze.
     * @cost O(K^2)
     */
    private void updateSequencesInDir(int[][] seqBoardDir, MNKCellState mark, Coord pos, Coord dir, MNKCellState target) {
        int backward = 1, forward = 1, size;
        boolean bwK = false, fwK = false;
        Coord inizio = new Coord(pos).sub(dir);
        Coord fine = new Coord(pos).add(dir);
        int[] s;

        while (backward < K && isValid(inizio) && (B[inizio.i][inizio.j] != (mark == opp(target) ? mark : opp(mark)))) {
            inizio.sub(dir);
            backward++;
        }
        bwK = backward == K && isValid(inizio) && B[inizio.i][inizio.j] != (mark == opp(target) ? mark : opp(mark));
        backward--;
        inizio.add(dir);

        while (forward < K && isValid(fine) && (B[fine.i][fine.j] != (mark == opp(target) ? mark : opp(mark)))) {
            fine.add(dir);
            forward++;
        }
        fwK = forward == K && isValid(fine) && B[fine.i][fine.j] != (mark == opp(target) ? mark : opp(mark));
        forward--;
        fine.sub(dir);

        if (mark == target) {
            size = forward + backward + 1;
            s = new int[size];
            if (size >= K) {
                calcVectStartToEnd(s, 0, backward - 1, inizio, dir, target);
                calcVectEndToStart(s, backward + 1, size - 1, fine, dir, target);
                if (backward <= 0)
                    s[0] = s[1];
                else if (forward <= 0)
                    s[size - 1] = s[size - 2];
                else
                    s[backward] = Math.max(s[backward - 1], s[backward + 1]);
                inizio = new Coord(pos).sub(dir.nmul(backward));
                for (int i = 0; i < size; i++, inizio.add(dir)){
                    if(seqBoardDir[inizio.i][inizio.j]>0 && (B[inizio.i][inizio.j]==MNKCellState.FREE||inizio.equals(pos)))
                        seqCount[mark==SufferingDoge.me?0:1][seqBoardDir[inizio.i][inizio.j]-1]--;
                    seqBoardDir[inizio.i][inizio.j] = Math.max(seqBoardDir[inizio.i][inizio.j], s[i]);
                    if(seqBoardDir[inizio.i][inizio.j]>0 && B[inizio.i][inizio.j]==MNKCellState.FREE)
                        seqCount[mark==SufferingDoge.me?0:1][seqBoardDir[inizio.i][inizio.j]-1]++;
                }
            }
        } else {
            size = forward + backward + 1;
            s = new int[size];
            s[backward] = -1;
            if (!bwK)
                for (int i = 0; i < backward; i++)
                    s[i] = -1;
            else
                calcVectStartToEnd(s, 0, backward - 1, inizio.nsub(dir), dir, target);
            if (!fwK)
                for (int i = 0; i < forward; i++)
                    s[size - 1 - i] = -1;
            else
                calcVectEndToStart(s, backward + 1, size - 1, fine.nadd(dir), dir, target);
            inizio = pos.nsub(dir.nmul(backward));
            for (int i = 0; i < size && isValid(inizio); i++, inizio.add(dir)) {
                if (seqBoardDir[inizio.i][inizio.j] > 0 && (B[inizio.i][inizio.j]==MNKCellState.FREE||inizio.equals(pos)))
                    seqCount[mark==SufferingDoge.me?1:0][seqBoardDir[inizio.i][inizio.j]-1]--; //non considero piu' la sequenza
                if(s[i] > 0 && B[inizio.i][inizio.j]==MNKCellState.FREE)
                    seqCount[mark==SufferingDoge.me?1:0][s[i]-1]++;
                seqBoardDir[inizio.i][inizio.j] = s[i];
            }
        }
    }

    /** 
     * Valuta la board in base alla lunghezza delle sequenze presenti
     * @cost O(K)
     */
    public long getScore() {
        if (gameState() == MNKGameState.DRAW)
            return 0;
        else if (gameState() == SufferingDoge.myWin) 
            return Long.MAX_VALUE;
        else if (gameState() == SufferingDoge.oppWin)
            return Long.MIN_VALUE;
        long score = 0;
        int exp = 1;
        for(int i=0; i<K-3; i++, exp+=2)
            score += Math.pow(5, exp)*(seqCount[0][i]-seqCount[1][i]);
        for(int i=K-3; i<K; i++, exp+=2)
            score += Math.pow(10, exp)*(seqCount[0][i]-seqCount[1][i]);
        return score;
    }

    /** 
     * Per ognuno dei due giocatori, data una posizione, calcola la lunghezza massima delle sequenze in 4 direzioni, 
     *  la direzione in cui la sequenza è massima e la somma delle lunghezze delle sequenze in 4 direzioni
     * @cost O(1)
     */
    public int[][] getBothPlayerMaxValDirSum(MNKCell c) {
        int[][] maxValDirSum = new int[][] { { -1, 0, 0 }, { -1, 0, 0 } };
        for (int p = 0; p < 2; p++)
            for (int d = 0; d < 4; d++) {
                if (maxValDirSum[p][0] < seqBoard[p][d][c.i][c.j]) {
                    // valore massimo e direzione massima
                    maxValDirSum[p][0] = seqBoard[p][d][c.i][c.j];
                    maxValDirSum[p][1] = d;
                }
                // somma in 4 direzioni di un giocatore
                maxValDirSum[p][2] += Math.max(0, seqBoard[p][d][c.i][c.j]);
            }
        return maxValDirSum;
    }
    /** 
     * Data una cella e il valore massimo di una sequenza,
     *  verifica nelle matrici di entrambi i giocatori se la sequenza appare almeno due volte.
     * @cost O(1)
     */
    private boolean isDoubleMove(MNKCell c, int[][][][] seqBoard, int absMax) {
        boolean firstTime = false;
        for (int p = 0; p < 2; p++)
            for (int d = 0; d < 4; d++)
                if (seqBoard[p][d][c.i][c.j] == absMax) {
                    if (firstTime)
                        return true;
                    else
                        firstTime = true;
                }
        return false;
    }
    /** 
     * Data una cella c, l'indice del giocatore che ha la sequenza massima, 
     *  l'indice della direzione in cui la sequenza è massima e il valore massimo,
     *  verifica se in seguito ad una mossa del giocatore avversario, la sequenza massima scompare.
     * @cost O(1)
     */
    private boolean hasMaxDisappeared(MNKCell c, int[][][][] seqBoard, int maxPlayerIdx, int maxDirIdx, int maxVal) {
        int nextI = c.i + dirs[maxDirIdx][0];
        int nextJ = c.j + dirs[maxDirIdx][1];
        if (isValid(nextI, nextJ) && seqBoard[maxPlayerIdx][maxDirIdx][nextI][nextJ] == maxVal)
            return false;
        nextI = c.i - dirs[maxDirIdx][0];
        nextJ = c.j - dirs[maxDirIdx][1];
        return !(isValid(nextI, nextJ) && seqBoard[maxPlayerIdx][maxDirIdx][nextI][nextJ] == maxVal);
    }
    /** 
     * Determina l'importanza della cella c considerando la sequenza massima, 
     *  se e' una mossa doppia e se la sequenza massima potrebbe venire annullata da una mossa avversaria.
     * @cost O(K^2)
     */
    public boolean determineImportance(int[][] maxValDirSum, boolean is_mine, MNKCell c) {
        // ho l'indice del giocatore massimo, valore massimo per ogni giocatore,
        // direzione massima per ogni giocatore
        int maxPlIdx = (maxValDirSum[0][0] >= maxValDirSum[1][0]) ? 0 : 1;

        if (FC.size() <= 1)
            return true;
        else if (maxPlIdx != ((is_mine) ? 1 : 0)) { // se il prossimo markcell puo' bloccare la sequenza massima
            boolean doubleMove = isDoubleMove(c, seqBoard, maxValDirSum[maxPlIdx][0]);
            markCell(c);
            boolean maxDisappeared = hasMaxDisappeared(c, seqBoard, maxPlIdx, maxValDirSum[maxPlIdx][1], maxValDirSum[maxPlIdx][0]);
            unmarkCell();
            return (doubleMove && maxDisappeared) || !maxDisappeared;
        }
        else {// posso testare la bonta' della mossa solo dopo 2 turni
            MNKCell random = null;
            for (MNKCell rand : getFreeCells()) 
                if (c.i != rand.i || c.j != rand.j) {
                    if(markCell(random = (new MNKCell(rand.i, rand.j)))==MNKGameState.OPEN)
                        break;
                    else{
                        unmarkCell();
                        random = null;
                    }
                }
            if(random==null)
                return true;
            unmarkCell();
            
            boolean doubleMove = isDoubleMove(c, seqBoard, maxValDirSum[0][0]);
            MNKCell randomMove = new MNKCell(random.i, random.j);
            if (markCell(randomMove) != mnkgame.MNKGameState.OPEN) {
                unmarkCell();
                return true;
            }
            markCell(c);
            boolean maxDisappeared = hasMaxDisappeared(c, seqBoard, maxPlIdx, maxValDirSum[maxPlIdx][1], maxValDirSum[maxPlIdx][0]);
            unmarkCell();
            unmarkCell();
            return doubleMove || !maxDisappeared;
        }
    }
}