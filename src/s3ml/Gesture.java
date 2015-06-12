/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import s3ml.DFA.DFA;
import s3ml.DFA.State;
import s3ml.DFA.Transition;

/**
 *
 * @author gnome3
 */
public class Gesture implements Serializable {

    private static final long serialVersionUID = 7863262235394607247L;

    public int symbol;
    public ArrayList<ArrayList<Integer>> msg;

    public Gesture(ArrayList<ArrayList<Integer>> msg, int symbol) {
        this.msg = msg;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(symbol);
        sb.append(" = ");

        sb.append(msg);

        return sb.toString();
    }

    public DFA getDFA(Map<ArrayList<Integer>, Integer> alphabetMap) {
        DFA dfa = new DFA();

        ArrayList<State> states = new ArrayList<>();

        State state = new State();
        state.startState = true;
        states.add(state);//0

        for (int i = 0; i < msg.size(); i++) {
            states.add(new State());
        }
        states.get(states.size() - 1).finalState = true;

        ArrayList<Transition> transitions = new ArrayList<>();

        State old = null;
        for (State s : states) {
            if (old != null) {
                transitions.add(new Transition(alphabetMap.get(msg.get(transitions.size())), old, s));
            }
            old = s;
        }

        dfa.states = states;
        dfa.transitions = transitions;

        return dfa;
    }

}
