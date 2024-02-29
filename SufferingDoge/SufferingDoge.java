package SufferingDoge;

import java.util.PriorityQueue;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import mnkgame.MNKPlayer;

public class SufferingDoge implements MNKPlayer {
    public static final int DEPTH = 9;
    private static final int MS_THRESHOLD = 1000;

    public static int M;
    public static int N;
    public static int K;
    public static MNKCellState me;
    public static MNKCellState opp;
    public static MNKGameState myWin;
    public static MNKGameState oppWin;
    public static int timeoutMs;
    public static long startMs;
    
    protected Node root;
    private Board board;
    
    /** 
     * @cost O(M*N)
     */
    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        SufferingDoge.M = M;
        SufferingDoge.N = N;
        SufferingDoge.K = K;
        SufferingDoge.timeoutMs = timeout_in_secs*1000;

        me = first?MNKCellState.P1:MNKCellState.P2;
        opp = first?MNKCellState.P2:MNKCellState.P1;
        myWin = first?MNKGameState.WINP1:MNKGameState.WINP2;
        oppWin = first?MNKGameState.WINP2:MNKGameState.WINP1;
        board = new Board(M, N, K);
        root = null;
    }

    /** 
     * Aggiorna la board con l'ultima mossa dell'avversario; se ancora non ve ne sono, marka la cella centrale.
     * Marka la cella migliore per il prossimo turno.
     * 
     * @cost O((DEPTH+1)!)
     */
    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        startMs = System.currentTimeMillis();
        if(root==null)
            return initTree(MC);
        else{
            selectCell(MC[MC.length-1], true);
            selectCell(root.bestChild, false);
            return root;
        }
    }
    /** 
     * @cost O((DEPTH+1)!)
     */
    private MNKCell initTree(MNKCell[] MC){
        MNKCell res = null;
        if(MC.length == 0){
            res = new MNKCell(board.M/2, board.N/2);
            int[][] maxValDirSum = board.getBothPlayerMaxValDirSum(res);
            int maxIdx = maxValDirSum[0][0] > maxValDirSum[1][0] ? 0 : 1;
            root = new Node(res, 0, 0, true, getImportance(maxValDirSum, maxIdx, true, new MNKCell(res.i, res.j)), maxValDirSum);
            board.markCell(root);
            board.removeLastSeq();
        }
        else{
            int[][] maxValDirSum = board.getBothPlayerMaxValDirSum(MC[0]);
            int maxIdx = maxValDirSum[0][0] > maxValDirSum[1][0] ? 0 : 1;
            root = new Node(MC[0], 0, 0, false, getImportance(maxValDirSum, maxIdx, false, new MNKCell(MC[0].i, MC[0].j)), maxValDirSum);
            board.markCell(root);
            launchAlphabeta();
            board.removeLastSeq();
            root = root.bestChild;
            board.markCell(root);
        }
        res = root;
        return res;
    }

    /**
     * @cost O((DEPTH+1)!)
     */
    private void selectCell(MNKCell cell, boolean runab){
        int[][] maxValDirSum = board.getBothPlayerMaxValDirSum(cell);
        int maxIdx = maxValDirSum[0][0] > maxValDirSum[1][0] ? 0 : 1;
        root = new Node(cell, 0, root.depth+1, !root.isMine, getImportance(maxValDirSum, maxIdx, !root.isMine, new MNKCell(cell.i, cell.j)), maxValDirSum);    
        board.markCell(root);
        board.removeLastSeq();
        if(runab)
            launchAlphabeta();
    }
    
    /**
     * @cost O(K^2) 
     */
    private boolean getImportance(int[][] maxValDirSum, int maxPlayerIdx, boolean isMine, MNKCell c){
        int abs_max = Math.max(maxValDirSum[0][0], maxValDirSum[1][0]);
        if(abs_max >= SufferingDoge.K-1)
            return true;
        else if(abs_max == SufferingDoge.K-2)
            return board.determineImportance(maxValDirSum, isMine, c);
        return false;
    }

    /**
     * @cost O((DEPTH+1)!)
     */
    private void launchAlphabeta(){
        alphabeta(root, Long.MIN_VALUE, Long.MAX_VALUE, DEPTH);
    }

    /**
     * @cost O(1) 
     */
    private boolean isLeaf(Node n, int remLevels){
        return board.gameState()!=MNKGameState.OPEN || remLevels == 0 || 
            System.currentTimeMillis() - SufferingDoge.startMs > SufferingDoge.timeoutMs - MS_THRESHOLD;
    }
    
    /**
     * @cost O((DEPTH+1)!) 
     */
    private long alphabeta(Node n, long alpha, long beta, int remLevels){
        if(isLeaf(n, remLevels)){
            n.score = board.getScore();
            return n.score;
        }
        else{
            PriorityQueue<Node> pq = new PriorityQueue<>(new NodeComparator());
            for(MNKCell c:board.getAdjacents()){
                int[][] maxValDirSum = board.getBothPlayerMaxValDirSum(c);
                int maxIdx = maxValDirSum[0][0] > maxValDirSum[1][0] ? 0 : 1;
                pq.add(new Node(c, 0L, n.depth+1, !n.isMine, getImportance(maxValDirSum, maxIdx, !root.isMine, new MNKCell(c.i, c.j)), maxValDirSum));
            }
            long eval;
            Node bestChild = null;
            if(!n.isMine){
                eval = Long.MIN_VALUE;
                for(int i=0; i<Math.max(5, remLevels+1) && !pq.isEmpty(); i++){
                    Node child = pq.poll();
                    board.markCell(child);
                    long abResult = alphabeta(child, alpha, beta, remLevels-1);
                    if(eval < abResult || (eval == abResult && bestChild == null)){
                        eval = abResult;
                        bestChild = child;
                    }
                    board.unmarkCell();
                    alpha = Math.max(alpha, eval);
                    if(beta <= alpha)
                        break;
                }
            }
            else{
                eval = Long.MAX_VALUE;
                for(int i=0; i<Math.max(5, remLevels+1) && !pq.isEmpty(); i++){
                    Node child = pq.poll();
                    board.markCell(child);
                    long abResult = alphabeta(child, alpha, beta, remLevels-1);
                    if(eval > abResult || (eval == abResult && bestChild == null)){
                        eval = abResult;
                        bestChild = child;
                    }
                    board.unmarkCell();
                    beta = Math.min(beta, eval);
                    if(beta <= alpha)
                        break;
                }
            }
            n.score = eval;
            n.bestChild = bestChild;
            return n.score;
        }
    }
    
    /** 
     * @cost O(1)
     */
    @Override
    public String playerName() {
        return "SufferingDoge";
    }
}
