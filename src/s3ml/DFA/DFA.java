/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml.DFA;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author gnome3
 */
public class DFA {

    private static int ID = 0;
    public int id;
    public ArrayList<State> states = new ArrayList<>();
    public ArrayList<Transition> transitions = new ArrayList<>();

    public DFA() {
        ID++;
        id = ID;
    }

    public DFA union(DFA a, DFA b) {
        return null;
    }

    public void minimize() {

    }

    public JFrame print(boolean visible) {
        DirectedSparseGraph<State, Transition> graph = new DirectedSparseGraph<>();

        for (State s : states) {
            graph.addVertex(s);
        }

        for (Transition t : transitions) {
            graph.addEdge(t, t.a, t.b, EdgeType.DIRECTED);
        }

        KKLayout<State, Transition> kkLayout = new KKLayout(graph);
        kkLayout.setSize(new Dimension(400, 300));

        final VisualizationViewer<State, Transition> vv = new VisualizationViewer<>(kkLayout);

        vv.getRenderContext().setVertexLabelTransformer(new Transformer<State, String>() {

            @Override
            public String transform(State i) {
                return i.name;
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

        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        final JFrame frame = new JFrame("V:" + graph.getVertexCount() + " E:" + graph.getEdgeCount());
        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(gm.getModeMenu());
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(visible);
        return frame;
    }

}
