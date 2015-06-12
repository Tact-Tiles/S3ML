/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import s3ml.DFA.DFA;

/**
 *
 * @author gnome3
 */
public class GestureEditor extends JPanel implements Step {

    Image img;
    double scale = 1;
    double pointSize = 50;
    private ArrayList<ArrayList<Integer>> selecteds = new ArrayList<>();
    static List<Gesture> gestures = new ArrayList<>();
    List<Integer> inputs = new ArrayList<>();
    Map<Integer, Ellipse2D> map = new HashMap<>();
    JPanel outerClassThis = this;
    private Settings s = null;
    JPanel top = new JPanel(new BorderLayout());
    final MyTableModel model = new MyTableModel();

    public GestureEditor() {
        super.setFocusable(true);

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (isVisible()) {
                            Thread.sleep(10);
                            repaint();
                        } else {
                            Thread.sleep(200);
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(S3ML.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();

        img = new ImageIcon(getClass().getClassLoader().getResource("resources/hand.png")).getImage();

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                outerClassThis.requestFocusInWindow();
                for (int e : inputs) {
                    if (map.get(e).contains(me.getX() / scale, me.getY() / scale)) {
                        if (me.isControlDown()) {
                            if (selecteds.isEmpty()) {
                                ArrayList<Integer> array = new ArrayList<>();
                                array.add(e);
                                selecteds.add(array);
                            } else {
                                ArrayList<Integer> array = selecteds.get(selecteds.size() - 1);
                                array.add(e);
                                Collections.sort(array);
                            }
                        } else {
                            ArrayList<Integer> array = new ArrayList<>();
                            array.add(e);
                            selecteds.add(array);
                        }
                        model.fireTableDataChanged();
                        return;
                    }
                }

//                        inputs.add(new Ellipse2D.Double(me.getX() / scale - pointSize / 2, me.getY() / scale - pointSize / 2, pointSize, pointSize));
            }

        };

        KeyAdapter ka = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent ke) {
                if (!selecteds.isEmpty()) {
                    switch (ke.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            String symbol = null;

                            while (symbol == null || symbol.equals("")) {
                                symbol = JOptionPane.showInputDialog("Qual o symbolo?");
                                if (symbol == null || symbol.equals("")) {
                                    JOptionPane.showMessageDialog(null, "Você não respondeu a pergunta.");
                                }
                            }
                            int s = Integer.parseInt(symbol);
                            gestures.add(0, new Gesture(selecteds, s));
                            selecteds = new ArrayList<>();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            selecteds = new ArrayList<>();
                            break;
                        case KeyEvent.VK_BACK_SPACE:
                            if (selecteds.get(selecteds.size() - 1).size() > 1) {
                                selecteds.get(selecteds.size() - 1).remove(selecteds.get(selecteds.size() - 1).size() - 1);
                            } else {
                                selecteds.remove(selecteds.size() - 1);
                            }
                            break;
                    }
                    model.fireTableDataChanged();
                }
            }
        };

        super.addMouseListener(ma);
        super.addKeyListener(ka);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.white);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (img != null) {
            {
                int w = img.getWidth(null);
                int h = img.getHeight(null);
                int W = getWidth();
                int H = getHeight();
                int image = 0;
                int panel = 0;
                if (H < W) {
                    panel = H;
                } else {
                    panel = W;
                }
                if (h > w) {
                    image = h;
                } else {
                    image = w;
                }
                scale = panel / (double) image;
            }
            g2.drawImage(img, 0, 0, (int) (img.getWidth(null) * scale), (int) (img.getHeight(null) * scale), null);

            g2.scale(scale, scale);
            g2.setFont(new Font("Sans", 10, 30));
            int i = 65;
            //desenho das entradas
            for (int e : inputs) {
                Ellipse2D el = map.get(e);
                //a entrada atual esta selecionada?
                boolean contains = false;
                for (ArrayList<Integer> a : selecteds) {
                    if (a.contains(e)) {
                        contains = true;
                        break;
                    }
                }
                if (contains) {
                    //entao precisamos desenhar todos as conexoes presentes
                    int index = 0;
                    int r = 10;
                    int n = 0;
                    //contamos quantas vezes essa entrada aparece
                    for (ArrayList<Integer> a : selecteds) {
                        for (int s : a) {
                            if (s == e) {
                                n++;
                            }
                        }
                    }
                    g2.translate(-n * r, -n * r);

                    for (ArrayList<Integer> a : selecteds) {
                        for (int s : a) {
                            if (s == e) {
                                g2.setColor(Color.getHSBColor((index / (float) selecteds.size()) * .25f, 1, 1));
                                g2.translate(r, r);
                                g2.fill(el);
                                g2.setColor(Color.black);
                                g2.draw(el);
                            }
                        }
                        index++;
                    }
                    g2.setColor(Color.black);
                    g2.drawString(" " + (char) i, (int) el.getX() + 5, (int) el.getY() + 35);
                } else {
                    //desenha simples
                    g2.setColor(Color.lightGray);
                    g2.fill(el);
                    g2.setColor(Color.black);
                    g2.draw(el);
                    g2.drawString(" " + (char) i, (int) el.getX() + 5, (int) el.getY() + 35);
                }
                i++;
            }
            g2.scale(1 / scale, 1 / scale);
        }

    }

    @Override
    public void build() {

        top.setName("Gesture Editor");
        final JTable table = new JTable(model);
        final JScrollPane scrollPane = new JScrollPane(table);

        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                    //System.out.println(table.getValueAt(table.getSelectedRow(), 0).toString());
                    selecteds = gestures.get(table.getSelectedRow()).msg;
                    Gesture g = gestures.get(table.getSelectedRow());
//                    DFA dfa = g.getDFA(s.alphabetMap);
//                    dfa.print(true);
                }
            }
        });

        TableColumn column;
        for (int i = 0; i < 2; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMaxWidth(60);
            } else if (i == 1) {
                column.setMinWidth(100);
            }
        }
        top.add(this, BorderLayout.CENTER);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        top.add(scrollPane, BorderLayout.PAGE_END);

    }

    @Override
    public void setSettings(Settings s) {
        this.s = s;
        inputs = s.getInputs();
        gestures = s.getGestures();
        map.clear();
        for (Map.Entry<Integer, List<Double>> e : s.getMap().entrySet()) {
            List<Double> v = e.getValue();
            map.put(e.getKey(), new Ellipse2D.Double(v.get(0), v.get(1), v.get(2), v.get(3)));
        }
        model.fireTableDataChanged();
    }

    @Override
    public JPanel getJPanel() {
        return top;
    }

    static class MyTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Symbol", "Gesture"};

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return gestures.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return gestures.get(row).symbol;
            } else if (col == 1) {
                return gestures.get(row).msg;
            }
            return "";
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    @Override
    public void doStep() {
        
    }

    @Override
    public boolean isDone() {
        return !gestures.isEmpty();
    }

    @Override
    public void setUndone() {

    }
}
