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
public class State {

    private static int ID = 0;
    public int id;
    public String name;
    public boolean startState = false;
    public boolean finalState = false;
    public int outputSymbol = 0;

    public State() {
        id = ID;
        ID++;
        name = "q" + id;
    }

    public State(String name) {
        this.name = name;
    }
}
