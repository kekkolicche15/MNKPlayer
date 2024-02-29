package SufferingDoge;

import mnkgame.MNKCell;

public class Coord {
    public int i;
    public int j;

    /** 
     * @cost O(1)
     */
    public Coord(MNKCell c){
        this(c.i, c.j);
    }
    /** 
     * @cost O(1)
     */
    public Coord(Coord c) {
        this(c.i, c.j);
    }
    /** 
     * @cost O(1)
     */
    public Coord(int i, int j) {
        this.i = i;
        this.j = j;
    }

    /** 
     * @cost O(1)
     */
    public Coord add(Coord c){
        i += c.i;
        j += c.j;
        return this;
    }
    /** 
     * @cost O(1)
     */
    public Coord sub(Coord c){
        i -= c.i;
        j -= c.j;
        return this;
    }
    /** 
     * @cost O(1)
     */
    public Coord nadd(Coord c){
        return new Coord(i+c.i, j+c.j);
    }
    /** 
     * @cost O(1)
     */
    public Coord nsub(Coord c){
        return new Coord(i-c.i, j-c.j);
    }
    /** 
     * @cost O(1)
     */
    public Coord nmul(int k){
        return new Coord(i*k, j*k);
    }

    /** 
     * @cost O(1)
     */
    public boolean equals(Object o){
        if(o instanceof Coord){
            Coord c = (Coord)o;
            return i==c.i && j==c.j;
        }
        return false;
    }
}
