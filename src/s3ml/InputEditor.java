/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author gnome3
 */
public class InputEditor extends JPanel implements Step {

    Image img;
    double scale = 1;
    double pointSize = 50;
    private int selected = -1;
    JPanel outerClassThis = this;
    Settings s = null;
    List<Integer> inputs = new ArrayList<>();
    Map<Integer, Ellipse2D> map = new HashMap<>();

    public InputEditor() {
        super.setName("Input Editor");
        super.setFocusable(true);

        super.setLayout(new FlowLayout());

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (outerClassThis.isVisible()) {
                            Thread.sleep(10);
//                                    System.out.println("####");
//                                    for (Ellipse2D e : inputs) {
//                                        System.out.println(e.getX() + ", " + e.getY());
//                                    }
//                                    System.out.println("####");
                            outerClassThis.repaint();
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
                        if (selected == e) {
                            selected = -1;
                        } else {
                            selected = e;
                        }
                        return;
                    }
                }
                map.put(inputs.size(), new Ellipse2D.Double(me.getX() / scale - pointSize / 2, me.getY() / scale - pointSize / 2, pointSize, pointSize));
                inputs.add(inputs.size());
            }

        };

        KeyAdapter ka = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent ke) {
                if (selected > 0) {
                    Rectangle2D f = map.get(selected).getFrame();
                    switch (ke.getKeyCode()) {
                        case KeyEvent.VK_UP:
                            map.get(selected).setFrame(f.getX(), f.getY() - 10, f.getWidth(), f.getHeight());
                            break;
                        case KeyEvent.VK_DOWN:
                            map.get(selected).setFrame(f.getX(), f.getY() + 10, f.getWidth(), f.getHeight());
                            break;
                        case KeyEvent.VK_LEFT:
                            map.get(selected).setFrame(f.getX() - 10, f.getY(), f.getWidth(), f.getHeight());
                            break;
                        case KeyEvent.VK_RIGHT:
                            map.get(selected).setFrame(f.getX() + 10, f.getY(), f.getWidth(), f.getHeight());
                            break;
                        case KeyEvent.VK_DELETE:
                            inputs.remove(selected);
                            selected = -1;
                            break;
                        case KeyEvent.VK_EQUALS:
                        case KeyEvent.VK_PLUS: {
                            int i = inputs.indexOf(selected);
                            if (i > 0) {
                                inputs.set(i, inputs.get(i - 1));
                                inputs.set(i - 1, selected);
                            }
                            break;
                        }
                        case KeyEvent.VK_MINUS: {
                            int i = inputs.indexOf(selected);
                            if (i < inputs.size() - 1) {
                                inputs.set(i, inputs.get(i + 1));
                                inputs.set(i + 1, selected);
                            }
                            break;
                        }
                    }
                }
            }
        };

        super.addMouseListener(ma);
        super.addKeyListener(ka);
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        //super.paintComponent(g);
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
            for (int e : inputs) {
                if (selected == e) {
                    g2.setColor(Color.magenta);
                } else {
                    g2.setColor(Color.yellow);
                }
                Ellipse2D s = map.get(e);
                if (s != null) {
                    g2.fill(s);
                    g2.setColor(Color.black);
                    g2.draw(s);
                    g2.drawString(" " + (char) i, (int) s.getX() + 5, (int) s.getY() + 35);
                }
                i++;
            }
            g2.scale(1 / scale, 1 / scale);
        }

    }

    @Override
    public void build() {

    }

    @Override
    public void setSettings(Settings s) {
        this.s = s;
        inputs = s.getInputs();
        map.clear();
        for (Map.Entry<Integer, List<Double>> e : s.getMap().entrySet()) {
            List<Double> v = e.getValue();
            map.put(e.getKey(), new Ellipse2D.Double(v.get(0), v.get(1), v.get(2), v.get(3)));
        }
    }

    @Override
    public JPanel getJPanel() {
        return this;
    }

    @Override
    public void doStep() {
        s.map.clear();
        for (Map.Entry<Integer, Ellipse2D> e : map.entrySet()) {
            Ellipse2D v = e.getValue();
            s.getMap().put(e.getKey(), Arrays.asList(v.getX(), v.getY(), v.getWidth(), v.getHeight()));
        }
        //isDone = true;
    }

    @Override
    public boolean isDone() {
        return true; //isDone;
    }

    @Override
    public void setUndone() {
        
    }
}
