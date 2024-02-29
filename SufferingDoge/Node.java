package SufferingDoge;

import java.util.Comparator;

import mnkgame.MNKCell;

class NodeComparator implements Comparator<Node>{

    @Override
    public int compare(Node n1, Node n2){
        return -n1.compareTo(n2);
    }
}
public class Node extends MNKCell{
    protected long score;
    protected int depth;
    protected boolean isMine;
    private boolean isImportant;
    private int maxPlayer;
    private int[][] maxValDirSum;
    protected Node bestChild;
    
    /** 
     * @cost O(1)
     */
    public Node(MNKCell c, long score, int depth, boolean isMine, boolean isImportant, int[][] maxValDirSum) {
        super(c.i, c.j, isMine?SufferingDoge.me:SufferingDoge.opp);
        this.score = score;
        this.depth = depth;
        this.isMine = isMine;
        this.isImportant = isImportant;
        this.maxValDirSum = maxValDirSum;
        this.maxPlayer = (maxValDirSum[0][0] > maxValDirSum[1][0])?0:1;
    }

    
    /** 
     * Compara due nodi in base alla loro priorita'.
     * Se entrambi sono importanti, l'ordine e' il seguente:
     * 1) massimo valore della somma di direzioni
     * 2) turno
     * 3) somma dei valori delle direzioni
     * Se entrambi non sono importanti, l'ordine e' il seguente:
     * 1) somma dei valori delle direzioni
     * 2) turno
     * 3) massimo valore della somma di direzioni
     * Se uno e' importante e l'altro no, l'importante viene prima.
     * @cost O(1)
     */
    public int compareTo(Node n){
        if(n==null)
            return 1;
        if(this.isImportant && n.isImportant){
            //priorita' max > turno > sum
            if(this.maxValDirSum[this.maxPlayer][0] == n.maxValDirSum[n.maxPlayer][0]){ //se massimo uguali
                int nextPlayer = (this.isMine)? 1 : 0;
                if((this.maxValDirSum[nextPlayer][0] == this.maxValDirSum[this.maxPlayer][0]  //se in tutti i e due il massimo e' del giocatore che giochera' la prossima mossa
                    && n.maxValDirSum[nextPlayer][0] == n.maxValDirSum[n.maxPlayer][0]) 
                    || !(this.maxValDirSum[nextPlayer][0] == this.maxValDirSum[this.maxPlayer][0]) 
                    && !(n.maxValDirSum[nextPlayer][0] == n.maxValDirSum[n.maxPlayer][0])){
                    if(this.maxValDirSum[0][2] + this.maxValDirSum[1][2] == n.maxValDirSum[0][2]+n.maxValDirSum[1][2])
                        return 0;
                    else if(this.maxValDirSum[0][2] + this.maxValDirSum[1][2] > n.maxValDirSum[0][2]+n.maxValDirSum[1][2])
                        return 1;
                    else
                        return -1;
                }
                else if(this.maxValDirSum[nextPlayer][0] == this.maxValDirSum[this.maxPlayer][0])
                    return 1;
                else
                    return -1;
                
            }
            else if(this.maxValDirSum[this.maxPlayer][0] > n.maxValDirSum[n.maxPlayer][0])
                return 1;
            else
                return -1;
            
        }
        else if(!this.isImportant && !n.isImportant){
            //priorita' sum > max > turno
            if(this.maxValDirSum[0][2] + this.maxValDirSum[1][2] == n.maxValDirSum[0][2]+n.maxValDirSum[1][2]){ //se somma uguali
                int nextPlayer = (this.isMine)? 1 : 0;
                if(this.maxValDirSum[this.maxPlayer][0] == n.maxValDirSum[n.maxPlayer][0]){ //se massimo uguali
                    if((this.maxValDirSum[nextPlayer][0] == this.maxValDirSum[this.maxPlayer][0] 
                        && n.maxValDirSum[nextPlayer][0] == n.maxValDirSum[n.maxPlayer][0]) 
                        || !(this.maxValDirSum[nextPlayer][0] == this.maxValDirSum[this.maxPlayer][0]) 
                        && (!(n.maxValDirSum[nextPlayer][0] == n.maxValDirSum[n.maxPlayer][0])))
                        return 0;
                    else if(this.maxValDirSum[nextPlayer][0] == this.maxValDirSum[this.maxPlayer][0])
                        return 1;
                    else
                        return -1;
                }
                else if(this.maxValDirSum[this.maxPlayer][0] > n.maxValDirSum[n.maxPlayer][0])
                    return 1;
                else
                    return -1;
                
            }
            else if(this.maxValDirSum[0][2] + this.maxValDirSum[1][2] > n.maxValDirSum[0][2]+n.maxValDirSum[1][2])
                return 1;
            else
                return -1;
        }
        return this.isImportant?1:-1;
    }
}