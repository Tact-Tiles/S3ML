/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml.DFA;

/**
 *
 * @author gnome3
 */
public class Transition {

    private static int ID = 0;
    public int id;
    public State a;
    public State b;
    public int symbol;

    public Transition() {
        ID++;
        id = ID;
    }

    public Transition(int symbol, State a, State b) {
        this.symbol = symbol;
        this.a = a;
        this.b = b;
    }

}
