/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import org.apache.commons.collections15.Transformer;
import s3ml.DFA.DFA;
import s3ml.DFA.State;
import s3ml.DFA.Transition;
import s3ml.util.JTextAreaPrintStream;

/**
 *
 * @author gnome3
 */
public class DFABuilder extends JPanel implements Step {

    private Settings s;
    private JTextArea textArea = new JTextArea();
    DirectedSparseGraph<State, Transition> graph = new DirectedSparseGraph<>();
    DFA dfa = new DFA();
    boolean isDone = false;

    public DFABuilder() {
        super.setName("DFA Builder");
        super.setLayout(new BorderLayout());

        textArea.setFont(new Font("monospaced", Font.PLAIN, 15));

        add(new JButton(new AbstractAction("Build DFA") {
            {
                setPreferredSize(new Dimension(200, 100));
            }

            @Override
            public void actionPerformed(ActionEvent ae) {
                new Thread() {
                    @Override
                    public void run() {
                        BuildDFA();
                    }
                }.start();
            }
        }), BorderLayout.PAGE_START);

        Layout<State, Transition> layout = new SpringLayout<>(graph);
        layout.setSize(new Dimension(1400, 1300));

        final VisualizationViewer<State, Transition> vv = new VisualizationViewer<>(layout);

        vv.getRenderContext().setVertexLabelTransformer(new Transformer<State, String>() {

            @Override
            public String transform(State i) {
                return i.name + "-" + i.outputSymbol;
            }

        });
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Transition, String>() {

            @Override
            public String transform(Transition i) {
                return "" + i.symbol;
            }

        });
        final Stroke stroke = new Stroke() {
            private Stroke stroke1, stroke2;

            {
                this.stroke1 = new BasicStroke(5f);
                this.stroke2 = new BasicStroke(1f);
            }

            @Override
            public Shape createStrokedShape(Shape shape) {
                return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
            }
        };
        vv.getRenderContext().setVertexStrokeTransformer(new Transformer<State, Stroke>() {
            @Override
            public Stroke transform(State i) {
                if (i.startState) {
                    return new BasicStroke(1.0f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER,
                            10.0f, new float[]{2.0f}, 0.0f);
                } else if (i.finalState) {
                    return stroke;
                } else {
                    return vv.getRenderContext().getGraphicsContext().getStroke();
                }
            }
        });

        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<State, Paint>() {
            @Override
            public Paint transform(State i) {
                if (i.finalState) {
                    return Color.RED;
                } else if (i.startState) {
                    return Color.CYAN;
                } else {
                    return Color.LIGHT_GRAY;
                }
            }
        });

        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    @Override
    public void build() {

    }

    @Override

    public void setSettings(Settings s) {
        this.s = s;
    }

    @Override
    public JPanel getJPanel() {
        return this;
    }

    public void BuildDFA() {
        if (s.alphabetMap.isEmpty()) {
            return;
        }

        dfa = new DFA();

        PrintStream out = new JTextAreaPrintStream(textArea);

        ArrayList<State> states = dfa.states;
        ArrayList<Transition> transitions = dfa.transitions;

        State startState = new State();
        startState.startState = true;
        transitions.add(new Transition(0, startState, startState));
        states.add(startState);//0

        boolean debounce = true;

        for (Gesture g : s.gestures) {
            int i  = 0;
            State lastState = startState;
            for (Iterator<ArrayList<Integer>> it = g.msg.iterator(); it.hasNext(); i++) {
                ArrayList<Integer> keyDescr = it.next();
                int k = this.s.alphabetMap.get(keyDescr);
                boolean stateExists = false;
                int j = 0;
                for (Transition t : transitions) {
                    if (t.a == lastState && t.symbol == k && i == j) {
                        stateExists = true;
                        lastState = t.b;
                        //break?
                        j++;
                    }
                }
                if (!stateExists) {
                    State newState = new State();
                    states.add(newState);
                    transitions.add(new Transition(k, lastState, newState));
                    if (debounce) {
                        transitions.add(new Transition(0, newState, newState));
//                        transitions.add(new Transition(k, newState, newState));
//                        out.println("State #" + newState.id + " debounce inputs:");
//                        for (ArrayList<Integer> ki : getAllCombinations(keyDescr)) {
//                            if (this.s.alphabetMap.containsKey(ki)){
//                                out.print(">");
//                            }
//                            out.println(ki);
//                        }
//                        out.println(" ");
                    }
                    lastState = newState;
//                    try {
//                        Thread.sleep(30);
//                    } catch (Exception e) {
//
//                    }
                }
                if (!it.hasNext()) {
                    lastState.finalState = true;
                    //transitions.add(new Transition(k, newState, newState));
                    lastState.outputSymbol = g.symbol;
                }
            }
        }

        out.println("Automaton size: " + states.size() + " states");
        out.println("Automaton size: " + (states.size() * (s.alphabetMap.size() + 1)) + " bytes");

        int inputSize = this.s.alphabetMap.size();
        List<Byte> data = s.data;
        int sindex = 1;
        for (State s : states) {
            s.id = sindex;
            sindex++;
        }

        for (State s : states) {
            out.print(String.format("%2d", (s.id - 1)) + ": ");
            for (int i = 0; i < inputSize + 1; i++) {
                boolean ok = false;
                for (Transition t : transitions) {
                    if (t.a == s && t.symbol == i) {
                        if (s.finalState && t.symbol == 0) {
                            int output = -(s.outputSymbol + 1);
                            if (data.get(1) == 1) {
                                data.add((byte) (output & 0xFF));
                                data.add((byte) (output >>> 8));
                            } else {
                                data.add((byte) output);
                            }
                            out.print("(" + String.format("%3d", output) + ")");
                        } else {
                            int id = t.b.id - 1;
                            if (data.get(1) == 1) {
                                data.add((byte) (id & 0xFF));
                                data.add((byte) (id >>> 8));
                            } else {
                                data.add((byte) id);
                            }
                            out.print("[" + String.format("%3d", id) + "]");
                        }
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    data.add((byte) -1);
                    if (data.get(1) == 1) {
                        data.add((byte) -1);
                    }
                    out.print("[   ]");
                }

            }
            out.println(" ");
        }

        dfa.print(true);
        //add(dfa.print(false).getRootPane(), BorderLayout.CENTER);
        updateUI();

        s.dumpMemory(out);
        isDone = true;
    }

    static <T> ArrayList<ArrayList<T>> allCombinations(ArrayList<T> array) {
        ArrayList<ArrayList<T>> r = new ArrayList<>();

        for (int i = 1; i < array.size(); i++) {
            for (int size = 1; size < array.size(); size++) {

            }
            ArrayList<T> tmp = new ArrayList<>();

            r.add(tmp);
        }

        return r;
    }

    public static void main(String[] args) {
        ArrayList<Integer> array = new ArrayList<>();
        array.add(0);
        array.add(1);
        array.add(2);
//        array.add(3);
//        array.add(4);
//        array.add(5);
//        array.add(6);
//        array.add(7);
//        array.add(8);
//        array.add(9);
        for (ArrayList<Integer> c : getAllCombinations(array)) {
            System.out.println(c);
        }
    }

    public static <T> ArrayList<ArrayList<T>> getAllCombinations(ArrayList<T> array) {
        ArrayList<ArrayList<T>> permutationsList = new ArrayList<>();
        combine(new ArrayList<T>(), array, permutationsList);
        return permutationsList;
    }

    public static <T> void combine(ArrayList<T> sub, ArrayList<T> array, ArrayList<ArrayList<T>> permutationsList) {
        if (sub.size() > 0) {
            permutationsList.add(sub);
        }
        if (array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                ArrayList<T> prefix = new ArrayList<>(sub);
                prefix.add(array.get(i));
                ArrayList<T> postfix = new ArrayList<>(array);
                postfix.remove(i);
                combine(prefix, postfix, permutationsList);
            }
        }
    }

    @Override
    public void doStep() {
        BuildDFA();
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void setUndone() {
        isDone = false;
    }
}
